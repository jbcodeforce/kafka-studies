package ut;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.labs.kc.containermgr.dao.CityDAO;
import ibm.labs.kc.containermgr.streams.ContainerInventoryView;
import ibm.labs.kc.containermgr.streams.ContainerOrderAssignment;
import ibm.labs.kc.model.Address;
import ibm.labs.kc.model.Container;
import ibm.labs.kc.model.Order;
import ibm.labs.kc.model.events.ContainerCreation;
import ibm.labs.kc.model.events.ContainerEvent;
import ibm.labs.kc.model.events.OrderEvent;
import ibm.labs.kc.utils.KafkaStreamConfig;
import ibm.labs.kc.utils.OrderProducer;


/**
 * 
 * 
 * A container is at a location with (long,lat) 
 * An order is defined with a pickup address and city, so tests
 * - no container in city
 * - one container match city
 * @author jerome boyer
 *
 */
public class TestContainerAssignment {
	private static CityDAO cityDao;
	private static ContainerOrderAssignment serv;
	private static ContainerInventoryView inventory;
	static TopologyTestDriver testDriver;
	static ConsumerRecordFactory<String, String> containerEventFactory;
	
	@BeforeClass
	public static void init() {
		cityDao = new CityDAO();
		serv = new ContainerOrderAssignment();
		inventory = (ContainerInventoryView)ContainerInventoryView.instance();
		
		Properties props = KafkaStreamConfig.getStreamsProperties("test");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
		
		testDriver = new TopologyTestDriver(
				inventory.buildProcessFlow(), props);
		containerEventFactory = new ConsumerRecordFactory<String, String>(KafkaStreamConfig.getContainerTopic(),
				new StringSerializer(), new StringSerializer());
	}
	
	
	@AfterClass
	public static void closing() {
		testDriver.close();
	}
	
	private ContainerCreation buildContainerEvent() {
		Container c = new Container("c01", "Brand", "Reefer",100, 37.8000,-122.25);
		c.setStatus("atDock");
		return  new ContainerCreation(ContainerEvent.CONTAINER_ADDED,"1.0",c);
	}
	
	@Test
	public void shouldContainerBeInCity() {
		Container c = new Container("c01", "Brand", "Reefer",100, 37.8000,-122.25);
		String city = cityDao.getCityName(c.getLatitude(), c.getLongitude());
		Assert.assertNotNull(city);
	}
	
	@Test
	public void shouldNotContainerBeInCity() {
		Container c = new Container("c01", "Brand", "Reefer",100, 45,-120);
		String city = cityDao.getCityName(c.getLatitude(), c.getLongitude());
		Assert.assertNull(city);
	}

	/**
	 * Test order assignment with service. As there is not containers added to the inventory 
	 * there no container id
	 */
	@Test
	public void testNoContainerInInventory(){
		OrderProducer op = new OrderProducer();
		OrderEvent orderEvent = op.buildOrderEvent();
		inventory.setStore(null);
		// normally the code should emit an event, but here we want to unit test without the event backbone
		String cid = serv.assignContainerToOrder(orderEvent.getPayload()).getContainerID();
		Assert.assertNull(cid);
	}
	

	
	private OrderEvent buildOrderEvent(){
		Address aSrc = new Address("street", "Oakland", "USA", "CA", "zipcode");
		Address aDest = new Address("street", "Singapore", "SGP", "", "zipcode");
		Order o = new Order(UUID.randomUUID().toString(),
	                "productID", "customerID", 1,
	                aSrc, "2019-01-10T13:30Z",
	                aDest, "2019-02-10T13:30Z",
	                Order.PENDING_STATUS);
		return new OrderEvent(new Date().getTime(),OrderEvent.TYPE_CREATED,"1.0",o);
	}
	
	
	private List<ConsumerRecord<byte[],byte[]>>  buildSomeContainerEventRecords() {

		ContainerCreation ce = buildContainerEvent();
		Gson parser = new Gson();
		
		ConsumerRecord<byte[],byte[]> record = containerEventFactory.create(KafkaStreamConfig.getContainerTopic(),ce.getContainerID(), parser.toJson(ce));
		List<ConsumerRecord<byte[],byte[]>> records = new ArrayList<ConsumerRecord<byte[],byte[]>>();
		records.add(record);
		ce = buildContainerEvent();
		ce.setContainerID("c02");
		ce.getPayload().setContainerID("c02");
		ce.getPayload().setLatitude(31.5);
		ce.getPayload().setLongitude(121.4);
		record = containerEventFactory.create(KafkaStreamConfig.getContainerTopic(),ce.getContainerID(), parser.toJson(ce));
		records.add(record);
		return records;
	}
	/**
	 * Using the kafka stream test driver to add containers and then verify one can be used 
	 * @throws InterruptedException
	 */
	@Test
	public void testContainerInInventoryMatchingCity() throws InterruptedException{
		// 1- add some containers to the inventory 
		testDriver.pipeInput(buildSomeContainerEventRecords());

		// So at this level the container is in the store
		Thread.sleep(5000);
		KeyValueStore<String, String> store = testDriver.getKeyValueStore(KafkaStreamConfig.CONTAINERS_STORE_NAME);
		inventory.setStore(store);
		// 2- Add an order event to get food from oakland
		OrderEvent orderEvent = buildOrderEvent();
		// normally the code should emit an event, but here we can unit test without the event backbone
		String cid = serv.assignContainerToOrder(orderEvent.getPayload()).getContainerID();
		Assert.assertNotNull(cid);
		Assert.assertEquals(cid,"c01");
	
	}
}
