package ibm.labs.kc.containermgr.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ibm.labs.kc.model.City;

/**
 * Data access to the cities having harbors and fields to keep containers.
 * 
 * @author jeromeboyer
 *
 */
public class CityDAO {
	private Map<String,City> cities;
	
	public CityDAO() {
		cities = new ConcurrentHashMap<String,City>();
		this.populateCities();
	}
	
	public void save(City c) {
		this.cities.put(c.getName(),c);
	}
	
	public City getCity(String name) {
		return cities.get(name);
	}
	
	public void populateCities() {
		Location[] rect = new Location[2];
		rect[0] = new Location(37.82932,-122.33771);
		rect[1] = new Location(37.76529,-122.24024);
		save(new City("Oakland", rect));
		rect = new Location[2];
		rect[0] = new Location(31.705410,121.237702);
		rect[1] = new Location(31.023246,121.962938);
		save(new City("Shanghai", rect));
		rect = new Location[2];
		rect[0] = new Location(40.725289,-74.031022);
		rect[1] = new Location(40.545150,-73.925350);
		save(new City("NYC", rect));
		rect = new Location[2];
		rect[0] = new Location(51.522663,-.162253);
		rect[1] = new Location(51.477619,-0.012422);
		save(new City("London", rect));
		
	}
	
	/**
	 * Return the city that includes the given longitude and latitude in a rectangle boundary
	 * @param latitude
	 * @param longitude
	 * @return a city name or null if the location is out of any known cities
	 */
	public String getCityName(double latitude, double longitude) {
		for (City c: this.cities.values()) {
			if (within(latitude,longitude,c.getLocations())) {
				return c.getName();
			}
		}
		return null;
	}
	
	/**
	 * This is a mock function to compute the latitude, longitude of a given city
	 * @param city
	 * @return latitude,longitude
	 */
	public Location getGeoLocation(String city) {
		City c = cities.get(city);
		return c.getLocations()[0];
	}
	
	
	private boolean within(double la, double lo,Location[] rect) {
		return ( ( la >= rect[1].latitude && la <= rect[0].latitude ) 
				 && (lo <= rect[1].longitude && lo >= rect[0].longitude));
	}
	
	public class Location {
		protected double latitude;
		protected double longitude;
		
		public Location(double la, double lo) {
			this.latitude = la;
			this.longitude = lo;
		}

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

		@Override
		public boolean equals(Object o) {
			Location p = (Location)o;
			return (this.getLatitude() == p.getLatitude() && this.getLongitude() == p.getLongitude());
			
		}
	}


}
