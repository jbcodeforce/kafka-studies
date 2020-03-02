package ut;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.labs.kc.model.Address;
import ibm.labs.kc.model.Container;
import ibm.labs.kc.model.Order;
import ibm.labs.kc.model.events.ContainerAssignment;
import ibm.labs.kc.model.events.ContainerCreation;
import ibm.labs.kc.model.events.ContainerEvent;
import ibm.labs.kc.model.events.OrderEvent;
import ibm.labs.kc.utils.KafkaStreamConfig;

public class TestOrderCreation {

	Gson parser = new Gson();
	static ConsumerRecordFactory<String, String> orderEventFactory;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		orderEventFactory = new ConsumerRecordFactory<String, String>("orders",
				new StringSerializer(), new StringSerializer());
	}

	private OrderEvent buildOrderEvent() {
		Address ap = new Address("main street", "Oakland","USA","CA","90000");
		Address ad = new Address("second street", "Shanghai","CHI","NE","03000");
		Order o = new Order("order-1","fresh product 01","tester01",2,ap,"30/03/2019",ad,"30/04/2019","pending");
		OrderEvent oe = new OrderEvent();
		oe.setType("OrderCreated");
		oe.setPayload(o);
		return oe;
	}
	
	@Test
	/**
	 * This test presenting a print of order event consumed from orders topic. It also illustrate that
	 * it is possible to send a json object as string for the value part of the record, and then use 
	 * a parser like Gson to get the object. 
	 */
	public void testBasicOrderPrint() {	
		System.out.println(" ***** testBasicOrderPrint");
		// build a topology that just get event from the stream, get the order and print the quantity ordered
		StreamsBuilder builder = new StreamsBuilder();
		
		builder.stream("orders")
 		.foreach((key,value) -> {
 			Order order = parser.fromJson((String)value, OrderEvent.class).getPayload();
 			System.out.println(value);
 			System.out.println(order.getOrderID() + " " + order.getQuantity());
 		});
		
		Properties props = KafkaStreamConfig.getStreamsProperties("test");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
		
		TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), props);
		ConsumerRecordFactory<String, String> factory = new ConsumerRecordFactory<String, String>("orders",
				new StringSerializer(), new StringSerializer());
		ConsumerRecord<byte[],byte[]> record = factory.create("orders","c01", parser.toJson(buildOrderEvent()));
		testDriver.pipeInput(record);
		testDriver.close();
	}

	
	@Test
	/**
	 * This test is extracting the order from the order event payload and publish the order to another topic. 
	 * It just adds only pending orders. 
	 */
	public void testValueTypeTransformation() {
		System.out.println(" ***** testValueTypeTransformation");
		StreamsBuilder builder = new StreamsBuilder();
		
		builder.stream("orders").flatMapValues((value) -> {
			 Order order = parser.fromJson((String)value, OrderEvent.class).getPayload();
			 List<String> results = new LinkedList<>();
			 if (order.getStatus().equals("pending")) {
				 results.add(parser.toJson(order));
			 }
			 return results;
		}).to("pendingOrders");
		
		TopologyTestDriver testDriver = buildTestDriver(builder);
		ConsumerRecordFactory<String, String> factory = new ConsumerRecordFactory<String, String>("orders",
				new StringSerializer(), new StringSerializer());
		OrderEvent oe = buildOrderEvent();
		ConsumerRecord<byte[],byte[]> record = factory.create("orders",oe.getPayload().getOrderID(), parser.toJson(oe));
		testDriver.pipeInput(record);
		ProducerRecord<String,String > recordOut = testDriver.readOutput("pendingOrders",new StringDeserializer(),new StringDeserializer());
		Assert.assertNotNull(recordOut);
		System.out.println(recordOut);
		Assert.assertTrue(oe.getPayload().getOrderID().equals(recordOut.key()));
		System.out.println(recordOut.key());
		Order o = parser.fromJson(recordOut.value(),Order.class);
		Assert.assertTrue(o.getOrderID().equals(oe.getPayload().getOrderID()));
		
		oe = buildOrderEvent();
		oe.getPayload().setStatus(("assigned"));
		oe.getPayload().setOrderID("c02");
		testDriver.pipeInput(factory.create("orders",oe.getPayload().getOrderID(), parser.toJson(oe)));
		ProducerRecord<String,String> recordOut2 = testDriver.readOutput("pendingOrders",new StringDeserializer(),new StringDeserializer());
		Assert.assertNull(recordOut2);
		testDriver.close();
	}
	
	@Test
	/**
	 * This test propose to have a container Map, organized by containerID and then getting information from the
	 * order to assign a container to the order.
	 */
	public void shouldAssignOneContainerToOrder() {
		System.out.println(" ***** shouldAssignOneContainerToOrder");
		StreamsBuilder builder = new StreamsBuilder();
		//KTable containers = populateContainerKTable(builder);
		builder.stream("orders").mapValues((orderEvent) -> {
			 Order order = parser.fromJson((String)orderEvent, OrderEvent.class).getPayload();
			 if (order.getStatus().equals("pending")) {
				 ContainerAssignment ca = new ContainerAssignment(order.getOrderID(),"c01");
				 return parser.toJson(ca);
			 }
			 return null;
		}).to("assignedOrders");
		
		TopologyTestDriver testDriver = buildTestDriver(builder);
		testDriver.pipeInput(buildOrderConsumerRecord());
		ProducerRecord<String,String > recordOut = testDriver.readOutput("assignedOrders",new StringDeserializer(),new StringDeserializer());
		Assert.assertNotNull(recordOut);
		System.out.println(recordOut);
	}
	
	private KTable populateContainerKTable(StreamsBuilder builder) {
		// use the default string serdes for key and value - the topology is just a table from the stream
		KTable containers=builder.table("containers",Materialized.as("queryable-container-store"));
		
		Properties props = KafkaStreamConfig.getStreamsProperties("test");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
		
		TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), props);
		ConsumerRecordFactory<String, String> factory = new ConsumerRecordFactory<String, String>("containers",
				new StringSerializer(), new StringSerializer());
		Container c = new Container("c01", "Brand", "Reefer",100, 37.8000,-122.25);
		ContainerCreation ce = new ContainerCreation(ContainerEvent.CONTAINER_ADDED,"1.0",c);
		ConsumerRecord<byte[],byte[]> record = factory.create("containers",
									ce.getPayload().getContainerID(), 
									parser.toJson(ce));
		testDriver.pipeInput(record);
		Container c2 = new Container("c02", "Brand", "Reefer",100, 50,100.25);
		ContainerCreation ce2 = new ContainerCreation(ContainerEvent.CONTAINER_ADDED,"1.0",c2);
		record = factory.create("containers",
									ce2.getPayload().getContainerID(), 
									parser.toJson(ce2));
		testDriver.pipeInput(record);
		testDriver.close();
		return containers;
	}
	
	private TopologyTestDriver buildTestDriver(StreamsBuilder builder) {
		Properties props = KafkaStreamConfig.getStreamsProperties("test");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
		return new TopologyTestDriver(builder.build(), props);	
	}
	
	private ConsumerRecord<byte[],byte[]> buildOrderConsumerRecord() {
		OrderEvent oe = buildOrderEvent();
		return  orderEventFactory.create("orders",oe.getPayload().getOrderID(), parser.toJson(oe));
	}
}
