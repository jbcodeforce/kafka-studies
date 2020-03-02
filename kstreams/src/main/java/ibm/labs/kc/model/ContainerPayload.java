package ibm.labs.kc.model;

public  class ContainerPayload {
	protected String containerID;
	protected String orderID; 
	
	public ContainerPayload() {}
	
	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}
	
	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderId) {
		this.orderID = orderId;
	}
}	 
