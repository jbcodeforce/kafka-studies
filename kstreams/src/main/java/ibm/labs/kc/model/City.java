package ibm.labs.kc.model;

import ibm.labs.kc.containermgr.dao.CityDAO.Location;

public class City {
	String name;
	Location[] locations = new Location[2];
	
	public City(String n,Location[] locations) {
		this.name = n;
		this.locations = locations;
	}
	
	public City(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location[] getLocations() {
		return locations;
	}

	public void setLocations(Location[] locations) {
		this.locations = locations;
	}
}
