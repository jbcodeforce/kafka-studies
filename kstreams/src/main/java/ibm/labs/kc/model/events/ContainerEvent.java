package ibm.labs.kc.model.events;

import java.util.Date;

public class ContainerEvent {

	// those are the list of event type
	public static final String CONTAINER_ADDED = "ContainerAdded";
	public static final String CONTAINER_REMOVED = "ContainerRemoved";
	public static final String CONTAINER_AT_LOCATION = "ContainerAtLocation";
	public static final String CONTAINER_ON_MAINTENANCE = "ContainerOnMaintenance";
	public static final String CONTAINER_OFF_MAINTENANCE =  "ContainerOffMaintenance";
	public static final String CONTAINER_ORDER_ASSIGNED = "ContainerAssignedToOrder";
	public static final String CONTAINER_ORDER_RELEASED = "ContainerReleasedFromOrder";
	public static final String CONTAINER_GOOD_LOADED = "ContainerGoodLoaded";
	public static final String CONTAINER_GOOD_UNLOADED = "ContainerGoodUnLoaded";
	public static final String CONTAINER_ON_SHIP = "ContainerOnShip";
	public static final String CONTAINER_OFF_SHIP = "ContainerOffShip";
	public static final String CONTAINER_ON_TRUCK = "ContainerOnTruck";
	public static final String CONTAINER_OFF_TRUCK = "ContainerOffTruck";
	protected long timestamp;
	protected String containerID;
	protected String type;
	protected String version;


	public ContainerEvent() {
		this.timestamp = (new Date()).getTime();
	}
	
	public ContainerEvent(String type) {
		this.type = type;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}
}
