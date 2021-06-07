package ibm.labs.kc.containermgr.streams;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Start the different kafka streams operators when the application context is created. This is needed
 * as this application is also exposing some REST api.
 * 
 * @author jeromeboyer
 *
 */
@WebListener
public class AppContextListener implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Initialize the Container consumer: listen to container events to build a unique data store
		ContainerInventoryView cView = (ContainerInventoryView)ContainerInventoryView.instance();
		cView.start();
		// listen to order event to search for matching containers
		ContainerOrderAssignment oAssignment = ContainerOrderAssignment.instance();
		oAssignment.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ContainerInventoryView cView = (ContainerInventoryView)ContainerInventoryView.instance();
		cView.stop();
		ContainerOrderAssignment oAssignment = ContainerOrderAssignment.instance();
		oAssignment.stop();
	}

}
