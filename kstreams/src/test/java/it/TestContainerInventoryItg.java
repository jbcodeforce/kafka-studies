package it;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ibm.labs.kc.containermgr.streams.ContainerInventoryView;
import ibm.labs.kc.model.Container;
import ibm.labs.kc.model.events.ContainerCreation;
import ibm.labs.kc.utils.ContainerProducer;

/**
 * This is an integration test, expecting to run against a local kafka
 * 
 * 
 * @author jeromeboyer
 *
 */
public class TestContainerInventoryItg {
	
	public static ContainerProducer externalContainerProducer;
	public static  ContainerInventoryView dao;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		externalContainerProducer = new ContainerProducer();
		// Start the container Streams process flow
		dao = (ContainerInventoryView)ContainerInventoryView.instance();
		dao.start();
	}
	
	@AfterClass
	public static void close() {
		externalContainerProducer.stop();
		dao.stop();
	}
	
	/*
	 * Produce container created event, and get the containers from API
	 */
	@Test
	public void shouldHaveContainerInTableInKafkaFromContainerCreatedEvent() throws InterruptedException, ExecutionException, TimeoutException {
		System.out.println(" ----- Kafka needs to run \n\tshouldHaveContainerInTableInKafkaFromContainerCreatedEvent");

		ContainerCreation ce = externalContainerProducer.buildContainerEvent();
	
		// verify the container does not exist in the inventory
		Container cOut = dao.getById(ce.getContainerID());
		Assert.assertNull(cOut);
		
		// emit container added event 
		externalContainerProducer.emit(ce);
		Thread.sleep(5000);
		// verify it is in the container store
		cOut = dao.getById(ce.getContainerID());
		Assert.assertNotNull(cOut);
		Assert.assertEquals(cOut.getStatus(),((Container)ce.getPayload()).getStatus());

	}

}
