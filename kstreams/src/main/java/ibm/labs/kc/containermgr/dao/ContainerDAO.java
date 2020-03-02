package ibm.labs.kc.containermgr.dao;

import java.util.List;

import ibm.labs.kc.model.City;
import ibm.labs.kc.model.Container;

public interface ContainerDAO {

	Container getById(String containerId);

	void start();

	List<Container> getAllContainers(City city);
	
	List<Container> getAllContainers(String city);

}
