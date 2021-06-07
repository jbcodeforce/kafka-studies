package ibm.labs.kc.model.events;

/*
 * This represents the payload for an OrderEvent and ContainerEvent
 */
public class ContainerAssignment extends ContainerEvent {
	 private String orderID;
	 
	 
	 public ContainerAssignment(String oid, String cid) {
		 super();
		 this.orderID = oid;
		 this.containerID = cid;
		 this.type = ContainerEvent.CONTAINER_ORDER_ASSIGNED;
	 }
	 
	 public ContainerAssignment(String oid, String cid, String type) {
		 super();
		 this.orderID = oid;
		 this.containerID = cid;
		 this.type = type;
	 }
	 
	 public String toString() {
		 return getOrderID() + " " + getContainerID();
	 }

	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	
}
