package it;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ibm.labs.kc.containermgr.streams.ContainerInventoryView;
import ibm.labs.kc.containermgr.streams.ContainerOrderAssignment;
import ibm.labs.kc.model.events.ContainerCreation;
import ibm.labs.kc.model.events.OrderEvent;
import ibm.labs.kc.utils.ContainerProducer;
import ibm.labs.kc.utils.OrderConsumer;
import ibm.labs.kc.utils.OrderProducer;

public class TestContainerToOrderAssignment {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void shouldHaveAContainerAssignedToOrder() throws InterruptedException {
	
		System.out.println(" ----- Kafka needs to run \n\t shouldHaveAContainerAssignedToOrder");
		// 1- start an order consumer to trace what's going on
		OrderConsumer oc = OrderConsumer.instance();
		oc.start();
		// 2- build the inventory
		ContainerInventoryView civ = (ContainerInventoryView)ContainerInventoryView.instance();
		civ.start();
		ContainerProducer cp = new ContainerProducer();
		ContainerCreation ce = cp.buildContainerEvent();
		ce.setContainerID("c02");
		ce.getPayload().setContainerID("c02");
		try {
			cp.emit(ce);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			fail("Could not emit container event");
		}
		// need to wait to be sure the container was added to the store
		Thread.sleep(5000);
		// 3- Add the order assignment, it listens to orders and find a container for each order
		ContainerOrderAssignment coa = ContainerOrderAssignment.instance();
		coa.start();
		// 4- create a new order for fresh food from Oakland
		OrderProducer op = new OrderProducer();
		OrderEvent oe= op.buildOrderEvent();
		try {
			op.emit(oe);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			fail("Could not emit order event");
		}
		Thread.sleep(3000);
		// 5- verify the container id and order id were generated
		Map<String,List<String>> events = oc.getHistory();
		Assert.assertNotNull(events);
		for (String orderId : events.keySet()) {
			System.out.println("Event order id:" + orderId);
			for (String o : events.get(orderId)) {
				System.out.println("\t" + o);
			}
		}
		cp.stop();
		op.stop();
		oc.stop();
		coa.stop();
		civ.stop();
	}

}
