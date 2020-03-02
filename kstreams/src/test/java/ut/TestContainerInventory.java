
package ut;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.labs.kc.containermgr.streams.ContainerInventoryView;
import ibm.labs.kc.model.Container;
import ibm.labs.kc.model.events.ContainerAssignment;
import ibm.labs.kc.model.events.ContainerCreation;
import ibm.labs.kc.model.events.ContainerEvent;
import ibm.labs.kc.utils.KafkaStreamConfig;

/**
 * Validate a set of expected behaviors for container inventory:
 * - on ContainerAdded event add new container to the inventory
 * - on ContainerAssigned to an order for example the container in inventory is updated with order id
 * @author jeromeboyer
 *
 */
public class TestContainerInventory {
	
	static Gson parser = new Gson();
	static ContainerInventoryView dao;
	static TopologyTestDriver testDriver;
	static ConsumerRecordFactory<String, String> containerEventFactory;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = (ContainerInventoryView)ContainerInventoryView.instance();
		// do not start the stream flow as we are using testDriver
		Properties props = KafkaStreamConfig.getStreamsProperties("test");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
		
		testDriver = new TopologyTestDriver(
				dao.buildProcessFlow(), props);
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
	
	@Before
	public void prepareData() {
		
	}
	
	@Test
	public void shouldNotHaveThisContainer() {
		KeyValueStore<String, String> store = testDriver.getKeyValueStore(KafkaStreamConfig.CONTAINERS_STORE_NAME);
		// inject the testing store, as we do not have started the real dao
		dao.setStore(store);
		
		Container container = dao.getById("c04");
		Assert.assertNull(container);
	}
	
	
	@Test
	public void shouldHaveContainerInTableFromContainerCreatedEvent() {
		
		ContainerCreation ce = buildContainerEvent();
		ConsumerRecord<byte[],byte[]> record = containerEventFactory.create(KafkaStreamConfig.getContainerTopic(),ce.getContainerID(), parser.toJson(ce));
		
		testDriver.pipeInput(record);
		
		KeyValueStore<String, String> store = testDriver.getKeyValueStore(KafkaStreamConfig.CONTAINERS_STORE_NAME);
		// inject the testing store
		dao.setStore(store);
		
		Container container = dao.getById(ce.getContainerID());
		Assert.assertNotNull(container);
		Assert.assertTrue(container.getContainerID().equals(ce.getContainerID()));
		Assert.assertTrue(container.getStatus().contains("atDock"));

		// now send a new event on the same key the container is updated in the table - keep last data
		((Container)ce.getPayload()).setStatus("onTruck");
		record = containerEventFactory.create("containers",ce.getContainerID(), parser.toJson(ce));
		testDriver.pipeInput(record);
		container = dao.getById(ce.getContainerID());
		Assert.assertFalse(container.getStatus().contains("atDock"));
		Assert.assertTrue(container.getStatus().contains("onTruck"));	
	}

	@Test
	public void shouldKeepPreviousFields() throws InterruptedException {
		// 1 - create a container
		ContainerCreation ce = buildContainerEvent();
		ce.setContainerID("c034");
		ce.getPayload().setContainerID(ce.getContainerID());
		ConsumerRecord<byte[],byte[]> record = containerEventFactory.create(KafkaStreamConfig.getContainerTopic(),ce.getContainerID(), parser.toJson(ce));
		testDriver.pipeInput(record);
		Thread.sleep(1000);
		// 2- verify attributes
		KeyValueStore<String, String> store = testDriver.getKeyValueStore(KafkaStreamConfig.CONTAINERS_STORE_NAME);
		// inject the testing store
		dao.setStore(store);
		
		Container container = dao.getById(ce.getContainerID());
		Assert.assertNotNull(container);
		Assert.assertNotNull(container.getStatus());
		Assert.assertTrue("NotAssigned".equals(container.getOrderID()));
		// 3- now inject a container assignment event 
		ContainerAssignment ca = new ContainerAssignment("order01",container.getContainerID());
		record = containerEventFactory.create(KafkaStreamConfig.getContainerTopic(),ce.getContainerID(), parser.toJson(ca));
		testDriver.pipeInput(record);
		Thread.sleep(3000);
		// 4- Verify only the orderID attribute was modified
		container = dao.getById(ce.getContainerID());
		Assert.assertTrue("order01".equals(container.getOrderID()));
		Assert.assertNotNull(container.getStatus());
		Assert.assertNotNull(container.getBrand());
		Assert.assertNotNull(container.getContainerID());
		Assert.assertNotNull(container.getType());
	}
	
}
