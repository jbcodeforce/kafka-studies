package acme.eda.demo.ordermgr.domain;

import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import acme.eda.demo.ordermgr.infra.events.Address;
import acme.eda.demo.ordermgr.infra.events.EventEmitter;
import acme.eda.demo.ordermgr.infra.events.EventType;
import acme.eda.demo.ordermgr.infra.events.OrderCreatedEvent;
import acme.eda.demo.ordermgr.infra.events.OrderEvent;
import acme.eda.demo.ordermgr.infra.repo.OrderRepository;
import acme.eda.demo.ordermgr.infra.repo.OrderRepositoryMem;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class OrderService {
	private static final Logger logger = Logger.getLogger(OrderService.class.getName());

	@Inject
	public OrderRepository repository;

	@Inject
	@Named("avroProducer")
	//@Named("default")
	public EventEmitter eventProducer;
	
	public OrderService(){}

	public OrderService(EventEmitter eventProducer, OrderRepositoryMem  repo) {
		this.eventProducer = eventProducer;
		this.repository = repo;
	}
	
	public OrderEntity createOrder(OrderEntity order) {
		// TODO This has to be transactional or use outbox, or use command pattern to kafka topic
		repository.addOrder(order);
		Address deliveryAddress = new Address(order.getDeliveryAddress().getStreet()
				,order.getDeliveryAddress().getCity()
				,order.getDeliveryAddress().getCountry()
				,order.getDeliveryAddress().getState(),
				order.getDeliveryAddress().getZipcode());
		OrderCreatedEvent orderPayload = new OrderCreatedEvent(order.getOrderID(),
				order.getProductID(),
				order.getCustomerID(),
				order.getQuantity(),
				order.getStatus(),
				deliveryAddress);
		OrderEvent orderEvent = new OrderEvent(order.getOrderID(),System.currentTimeMillis(),
				EventType.OrderCreated,orderPayload);
		
		try {
			logger.info("emit event for " + order.getOrderID());
			eventProducer.emit(orderEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return order;
	}

	public List<OrderEntity> getAllOrders() {
		return repository.getAll();
	}

	public OrderEntity findById(String id) {
        return repository.findById(id);
    }
	
    public void updateOrder(OrderEntity entity) {
		repository.updateOrder(entity);
    }

	public void onStart(@Observes StartupEvent ev){
		eventProducer.init();
	}

}
