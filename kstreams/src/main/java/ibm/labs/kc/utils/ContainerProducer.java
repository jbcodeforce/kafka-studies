package ibm.labs.kc.utils;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.google.gson.Gson;

import ibm.labs.kc.model.Container;
import ibm.labs.kc.model.events.ContainerCreation;
import ibm.labs.kc.model.events.ContainerEvent;

public class ContainerProducer {
	private KafkaProducer<String, String> kafkaProducer;
	 
	public ContainerProducer() {
		 Properties properties = KafkaStreamConfig.getProducerProperties("container-producer");
	     kafkaProducer = new KafkaProducer<String, String>(properties);
	}

	public ContainerCreation buildContainerEvent() {
		Container c = new Container(UUID.randomUUID().toString(), "IntegrationTestBrand", "Reefer",100, 37.8000,-122.25);
		c.setStatus("atDock");
		return  new ContainerCreation(ContainerEvent.CONTAINER_ADDED,"1.0",c);
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		ContainerProducer p = new ContainerProducer();
		p.emit(p.buildContainerEvent());
	}

	
	public void emit(ContainerCreation e) throws InterruptedException, ExecutionException, TimeoutException {		
		String key = e.getPayload().getContainerID();
		
		String value = new Gson().toJson(e);
	    ProducerRecord<String, String> record = new ProducerRecord<>(KafkaStreamConfig.getContainerTopic(), key, value);

	    Future<RecordMetadata> send = kafkaProducer.send(record);
	    send.get(KafkaStreamConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
	    System.out.println(" Emit container event " + e.getPayload().getContainerID());
	    
	}

	public void stop() {
		kafkaProducer.close();
	}
}
