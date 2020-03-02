package ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import ibm.labs.kc.containermgr.dao.CityDAO;
import ibm.labs.kc.model.City;

public class TestCityLocation {
	
	private static CityDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = new CityDAO();
	}

	@Test
	public void testCityMatch() {
		String city = dao.getCityName(37.8000,-122.25);
		assertNotNull(city);
		assertEquals("Oakland",city);
	}
	
	@Test
	public void testCityUnMatch() {
		String city = dao.getCityName(38.8000,-124.25);
		assertNull(city);
	}

	@Test
	public void getCity() {
		City c = dao.getCity("NYC");
		assertNotNull(c);
		assertEquals("NYC",c.getName());
	}
	
}
