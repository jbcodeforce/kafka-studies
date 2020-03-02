package ibm.labs.kc.containermgr.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.labs.kc.containermgr.dao.ContainerDAO;
import ibm.labs.kc.model.City;
import ibm.labs.kc.model.Container;
import ibm.labs.kc.model.events.ContainerAssignment;
import ibm.labs.kc.model.events.ContainerCreation;
import ibm.labs.kc.model.events.ContainerEvent;
import ibm.labs.kc.utils.KafkaStreamConfig;

/**
 * Process container events to build a container inventory view.
 * This implementation uses Kafka Streams APIs.
 * To keep last state of the container the approach is to use a KTable. 
 * 
 * @author jeromeboyer
 *
 */
public class ContainerInventoryView  implements ContainerDAO {
	private static final Logger logger = LoggerFactory.getLogger(ContainerInventoryView.class);
		
	private static ContainerInventoryView  instance;
	private KafkaStreams streams;
	private Gson jsonParser = new Gson();

	
	public synchronized static ContainerDAO instance() {
        if (instance == null) {
            instance = new ContainerInventoryView();
        }
        return instance;
    }
	
	/**
	 * Update container inventory with new container data 
	 * or update existing container record with received payload .
	 * @return
	 */
	public  Topology buildProcessFlow() {
		final StreamsBuilder builder = new StreamsBuilder();
	   
	    builder.stream(KafkaStreamConfig.getContainerTopic()).mapValues((containerEvent) -> {
	    		 // the container payload is of interest to keep in table
	   			Container c = manageContainerUpdate((String)containerEvent);
	   			 return jsonParser.toJson(c);
	   		 }).groupByKey()
	   		 	.reduce((c,container) -> {
	   		 		System.out.println("In streams for container topic received container " + container );
	   		 		
	   		 		return container;
	   		 	},
	   	    	  Materialized.as(KafkaStreamConfig.CONTAINERS_STORE_NAME));
	    return builder.build();
	}
	
	public  synchronized void start() {
		if (streams == null) {
			Properties props = KafkaStreamConfig.getStreamsProperties("container-streams-" + UUID.randomUUID().toString());
		    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		    streams = new KafkaStreams(buildProcessFlow(), props);
			try {
	        	streams.cleanUp(); 
	            streams.start();
	        } catch (Throwable e) {
	           e.printStackTrace();
	        }
			logger.info("ContainerInventoryView started");
		}
	}
	
	public void stop() {
		streams.close();
	}

	@Override
	public Container getById(String containerId) {
		if (getStore() != null) {
			ReadOnlyKeyValueStore<String,String> view =  getStore();
			String cStrg = view.get(containerId);
			if (cStrg != null) 
				return jsonParser.fromJson(cStrg, Container.class);
		} 
		return null;
	}

	/**
	 * Get all the container within a city.
	 * ATTENTION this is not implemented by city now, it is just all containers
	 */
	@Override
	public List<Container> getAllContainers(City city) {
		List<Container> l = new ArrayList<Container>();
		if (getStore() != null) {
			try {
				KeyValueIterator<String,String> k =  getStore().all();
				while (k.hasNext()) {
					l.add(jsonParser.fromJson(k.next().value, Container.class));
				}
			} catch(InvalidStateStoreException e) {
				e.printStackTrace();
				return l;
			}	
		}
		return l;
	}
	
	// ATTENTION this is not implemented by city now, it is just all containers
	@Override
	public List<Container> getAllContainers(String city) {
		return this.getAllContainers(new City(city));
	}

	ReadOnlyKeyValueStore<String,String> store ;
	
    public ReadOnlyKeyValueStore<String,String> getStore(){
    	if ( store == null && streams != null ) {
    		try {
    		store = streams.store(KafkaStreamConfig.CONTAINERS_STORE_NAME, QueryableStoreTypes.keyValueStore());
    		} catch (InvalidStateStoreException e) {
    			// until the stream process flow is started it is possible the store does not exist.
    			store = null;
    		}
    	}
    	return store;
    }
    
    public void setStore(ReadOnlyKeyValueStore<String,String> store) {
    	this.store = store;
    }
    
    /**
     * Depending of the event type the workload will not have the same content. To avoid erasing previous 
     * data we need to take care of payload content
     * @param containerEvent
     * @return container
     */
    private Container manageContainerUpdate(String containerEvent) {
    	ContainerEvent ce = jsonParser.fromJson(containerEvent, ContainerEvent.class);
    	
		switch (ce.getType()) {
			case ContainerEvent.CONTAINER_ORDER_ASSIGNED: 
				ContainerAssignment ca = jsonParser.fromJson(containerEvent, ContainerAssignment.class);
				Container c = getById(ca.getContainerID());
				c.setOrderID(ca.getOrderID());
				return c;
			case ContainerEvent.CONTAINER_ADDED:
			default:
				ContainerCreation cc = jsonParser.fromJson(containerEvent, ContainerCreation.class);
				c = cc.getPayload();
				return c;
		}
		 
    }


}
