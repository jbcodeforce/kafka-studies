package ibm.labs.kc.model.events;

import ibm.labs.kc.model.Order;

public class OrderEvent extends AbstractEvent {

    public static final String TYPE_CREATED = "OrderCreated";
    public static final String TYPE_UPDATED = "OrderUpdated";
    public static final String TYPE_BOOKED = "OrderBooked";
    public static final String TYPE_ASSIGNED = "OrderAssigned"; // from voyage ms
    public static final String TYPE_TRANSIT = "OrderInTransit";
    public static final String TYPE_COMPLETED = "OrderCompleted";
    public static final String TYPE_REJECTED = "OrderRejected";
    public static final String TYPE_CANCELLED = "OrderCancelled";
   
    public static final String TYPE_CONTAINER_ALLOCATED = "ContainerAllocated";
    public static final String TYPE_FULL_CONTAINER_VOYAGE_READY = "FullContainerVoyageReady";
    public static final String TYPE_CONTAINER_ON_SHIP = "ContainerOnShip";
    public static final String TYPE_CONTAINER_OFF_SHIP = "ContainerOffShip";
    public static final String TYPE_CONTAINER_DELIVERED = "ContainerDelivered";
    private Order payload;

    public OrderEvent(long timestampMillis, String type, String version, Order o) {
        super(timestampMillis, type, version);
        this.payload = o;
    }

    public OrderEvent() {}


    @Override
	public Order getPayload() {
		return payload;
	}

	public void setPayload(Order payload) {
		this.payload = payload;
	}

}
