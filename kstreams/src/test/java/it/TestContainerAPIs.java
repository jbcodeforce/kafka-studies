package it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ibm.labs.kc.model.Container;

public class TestContainerAPIs {

	private String port = System.getProperty("liberty.test.port");
	private String endpoint = "/containers";
	private String url = "http://localhost:" + port + endpoint;
	private Gson parser = new Gson();

	 
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGettingAllContainers() throws InterruptedException {
		System.out.println("Testing endpoint " + url);
        int maxCount = 10;
        int responseCode = 0;
        for (int i = 0; (responseCode != 200) && (i < maxCount); i++) {
            Client client = ClientBuilder.newClient();
            Invocation.Builder invoBuild = client.target(url+"/city/Oakland").request();
            Response response = invoBuild.get();
        
            if (response.hasEntity()) {
            	 String containerssAsString=response.readEntity(String.class);
            	 assertNotNull(containerssAsString);
			      try{
				      Container[] containers = parser.fromJson(containerssAsString,Container[].class);
				     
				      for (Container c : containers) {
				    	  System.out.println(c.toString());
				      }
			      }catch(IllegalStateException | JsonSyntaxException exception){
			    	  System.out.println(exception);
			      }
            }
            System.out.println("Response code : " + responseCode + ", retrying ... (" + i + " of " + maxCount + ")");
            responseCode = response.getStatus();
            response.close();
            Thread.sleep(5000);
        }
        assertTrue("Incorrect response code: " + responseCode, responseCode == 200);
        
	}

}
