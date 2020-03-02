package ibm.labs.kc.model.events;

import ibm.labs.kc.model.Container;

public class ContainerCreation extends ContainerEvent {
	
	protected Container payload;
	
	public ContainerCreation() {
		super();
	}
	
	public ContainerCreation(String type,String version, Container p) {
		super();
		this.containerID = p.getContainerID();
		this.type = ContainerEvent.CONTAINER_ADDED;
		this.version = version;
		this.payload = p;
	}

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

	
	
}
