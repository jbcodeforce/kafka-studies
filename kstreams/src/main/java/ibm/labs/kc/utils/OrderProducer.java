package ibm.labs.kc.utils;

import java.util.Date;
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

import ibm.labs.kc.model.Address;
import ibm.labs.kc.model.Order;
import ibm.labs.kc.model.events.OrderEvent;

public class OrderProducer {
	private KafkaProducer<String, String> kafkaProducer;
	 
	public OrderProducer() {
		 Properties properties = KafkaStreamConfig.getProducerProperties("order-producer");
	     kafkaProducer = new KafkaProducer<String, String>(properties);
	}

	public OrderEvent buildOrderEvent(){
		Address aSrc = new Address("street", "Oakland", "USA", "CA", "zipcode");
		Address aDest = new Address("street", "Singapore", "SGP", "", "zipcode");
		Order o = new Order(UUID.randomUUID().toString(),
	                "productID", "customerID", 1,
	                aSrc, "2019-01-10T13:30Z",
	                aDest, "2019-02-10T13:30Z",
	                Order.PENDING_STATUS);
		return new OrderEvent(new Date().getTime(),OrderEvent.TYPE_CREATED,"1.0",o);
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		OrderProducer p = new OrderProducer();
		OrderEvent oe = p.buildOrderEvent();
		p.emit(oe);
	}

	public void emit(OrderEvent oe) throws InterruptedException, ExecutionException, TimeoutException {
		
		String key = oe.getPayload().getOrderID();
		
		String value = new Gson().toJson(oe);
	    ProducerRecord<String, String> record = new ProducerRecord<>(KafkaStreamConfig.getOrderTopic(), key, value);

	    Future<RecordMetadata> send = kafkaProducer.send(record);
	    send.get(KafkaStreamConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
	    System.out.println(" Emit order event " + oe.getPayload().getOrderID());

	}

	public void stop() {
	    kafkaProducer.close();
	}
}
