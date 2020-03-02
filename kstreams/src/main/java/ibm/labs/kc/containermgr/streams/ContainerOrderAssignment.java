/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ibm.labs.kc.containermgr.streams;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.labs.kc.containermgr.dao.CityDAO;
import ibm.labs.kc.model.City;
import ibm.labs.kc.model.Container;
import ibm.labs.kc.model.Order;
import ibm.labs.kc.model.events.OrderEvent;
import ibm.labs.kc.utils.JsonPOJODeserializer;
import ibm.labs.kc.utils.JsonPOJOSerializer;
import ibm.labs.kc.utils.KafkaStreamConfig;

/**
 * This is the container to order assignment 'service' implemented using streams.
 * 
 * 
 */
public class ContainerOrderAssignment {
	private static final Logger logger = LoggerFactory.getLogger(ContainerOrderAssignment.class);
	// as there is only one thread consuming message in this code, singleton could have been avoided.
    public static ContainerOrderAssignment instance;

	private KafkaStreams streams;
	// As of now this is a mockup
	private CityDAO cityDAO = new CityDAO();
    

    public synchronized static ContainerOrderAssignment instance() {
        if ( instance == null ) {
            instance = new ContainerOrderAssignment();
        }
        return instance;
    }

    public synchronized void start() {
        if ( streams == null ) {
            Properties props = KafkaStreamConfig.getStreamsProperties("containerToOrder-streams");
		    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		    streams = new KafkaStreams(buildProcessFlow(), props);
			try {
	        	streams.cleanUp(); 
	            streams.start();
	        } catch (Throwable e) {
	            System.exit(1);
	        }
			logger.info("ContainerOrderAssignment started");
        }
    }

    public synchronized void stop() {
        if ( streams == null ) {
            streams.close();
        }
    }
    
    private Serde<OrderEvent> buildOrderEventSerde() {
		Map<String, Object> serdeProps = new HashMap<>();
		final Serializer<OrderEvent> orderEventSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", OrderEvent.class);
        orderEventSerializer.configure(serdeProps, false);
        final Deserializer<OrderEvent> orderEventDeserializer = new JsonPOJODeserializer<>();
        orderEventDeserializer.configure(serdeProps, false);
        
        return Serdes.serdeFrom(orderEventSerializer, orderEventDeserializer);
	}
    
    /**
     * Get the pending order or order with voyage and search a container from the inventory based on location and time to pickup
     * Assign the container id to the order to generate a ContainerAssignment event to the orders topics
     * Send the same event to containers topic too so the inventory is updated.
     * If there is no container available put the order on hold and send the order event rejected
     * @return the topology for the process flow
     */
	public Topology buildProcessFlow() {
		final StreamsBuilder builder = new StreamsBuilder();
	  
	        KStream<String,OrderEvent>[] branches = builder.stream(KafkaStreamConfig.getOrderTopic(),Consumed.with(Serdes.String(), buildOrderEventSerde()))
	        		.filter((key,orderEvent) -> {
	        			   return (OrderEvent.TYPE_BOOKED.equals(orderEvent.getType())); 
	        		})
	        		.mapValues((key,orderEvent) -> {
	        			 Order orderOut=assignContainerToOrder(orderEvent.getPayload());
	        			 orderEvent.setPayload(orderOut);
	        			 return orderEvent;
	        		})
	        		.branch((key,orderEvent) -> {
	        				Order order =  orderEvent.getPayload();
	        				return (order.getContainerID() == null);
	        				},
	        				(key,orderEvent) -> true);
            
	        branches[0].mapValues(orderEvent -> {
	        	orderEvent.getPayload().setStatus(Order.ONHOLD_STATUS);
	        	orderEvent.setType(OrderEvent.TYPE_REJECTED);
	        	return orderEvent;
	        }).through(KafkaStreamConfig.getRejectedOrdersTopic());
	        
	        branches[1].mapValues(orderEvent -> {
	        	orderEvent.getPayload().setStatus(Order.CONTAINER_ALLOCATED_STATUS);
	        	orderEvent.setType(OrderEvent.TYPE_CONTAINER_ALLOCATED);
	        	return orderEvent;
	        }).through(KafkaStreamConfig.getAllocatedOrdersTopic())
	        .mapValues(orderEvent -> {
	        		Container c = new Container();
	        		c.setContainerID(orderEvent.getPayload().getContainerID());
	        		c.setOrderID(orderEvent.getPayload().getOrderID());
	        		return c;
		        }).through(KafkaStreamConfig.getContainerTopic());
	        
	        return builder.build();
	}
	
	public Order assignContainerToOrder(Order order) {
		
		City city = this.cityDAO.getCity(order.getPickupAddress().getCity());
		// this is not the best implementation but as of now we have few containers so we can do that
		for (Container c : ContainerInventoryView.instance().getAllContainers(city)) {
			// test the capacity and the type... so here we do nothing, just use the first container not allocated
			if (c.getOrderID() == null || "NotAssigned".equals(c.getOrderID())) {
				c.setOrderID(order.getOrderID());
				order.setContainerID(c.getContainerID());
				return order;
			}
		}
		return order;
	}
	
}
