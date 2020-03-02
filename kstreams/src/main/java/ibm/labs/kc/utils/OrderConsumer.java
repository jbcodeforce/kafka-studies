package ibm.labs.kc.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

public class OrderConsumer {
	private static OrderConsumer instance;
	private KafkaStreams streams;
	
	private Map<String,List<String>> history = new HashMap<String,List<String>>();
	
	public  Topology buildProcessFlow() {
		 final StreamsBuilder builder = new StreamsBuilder();
	        builder.stream("orders")
	        		.foreach((key,value) -> {
	        			System.out.println("In order stream consumer received order " + key + " => " + value);
	        			if (this.history.get(key) == null) {
	        				List<String> l = new ArrayList<String>();
	        				l.add((String)value);
	        				this.history.put((String) key, l);
	        			} else {
	        				this.history.get(key).add((String) value);
	        			}
	        		});

	        return builder.build();
	}
	
	public synchronized static OrderConsumer instance() {
        if ( instance == null ) {
            instance = new OrderConsumer();
        }
        return instance;
    }

    public synchronized void start() {
        if ( streams == null ) {
            Properties props = KafkaStreamConfig.getStreamsProperties("order-streams");
		    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		    final Topology topology = buildProcessFlow();
	        System.out.println(topology.describe());
		    streams = new KafkaStreams(topology, props);
			try {
	        	streams.cleanUp(); 
	            streams.start();
	        } catch (Throwable e) {
	            System.exit(1);
	        }
        }
    }

    public synchronized void stop() {
        if ( streams == null ) {
            streams.close();
        }
    }
	
	public static void main(String[] args) {
		OrderConsumer oc = OrderConsumer.instance();
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
            	oc.stop();
                latch.countDown();
            }
        });

        try {
        	oc.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

	public Map<String,List<String>> getHistory() {
		return history; 
	}
}
