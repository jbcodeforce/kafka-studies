package ibm.labs.kc.model;

public class Container extends ContainerPayload {

	protected double latitude;
	protected double longitude;
	protected String type;
	protected String status;
	protected String brand;
	protected int capacity;
	
	public Container(String cid, String brand, String type, int capacity, double lat, double lo) {
		this.containerID = cid;
		this.type = type;
		this.latitude = lat;
		this.brand = brand;
		this.capacity = capacity;
		this.longitude = lo;
		this.orderID = "NotAssigned";
	}
	
	// need default constructor for jackson deserialization
	public Container() {}
	



	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}


	
}
