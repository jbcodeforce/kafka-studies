package acme.eda.demo.ordermgr.infra.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

import acme.eda.demo.ordermgr.domain.OrderEntity;
import acme.eda.demo.ordermgr.domain.OrderService;
import acme.eda.demo.ordermgr.infra.api.dto.OrderDTO;
import io.smallrye.mutiny.Uni;

@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OrderResource {
    private static final Logger logger = Logger.getLogger(OrderResource.class.getName());

    @Inject
    public OrderService service;
    
    @GET
    public List<OrderDTO> getAllActiveOrders() {
        List<OrderDTO> l = new ArrayList<OrderDTO>();
        for (OrderEntity order : service.getAllOrders()) {
            l.add(OrderDTO.fromEntity(order));
        }
        return l;
    }


    @GET
    @Path("/{id}")
    public OrderDTO get(@PathParam("id") String id) {
        logger.info("In get order with id: " + id);
        OrderEntity order = service.findById(id);
        if (order == null) {
            throw new WebApplicationException("Order with id of " + id + " does not exist.", 404);
     
        }
        return OrderDTO.fromEntity(order);
    }

    @POST
    public OrderDTO saveNewOrder(OrderDTO order) {
        OrderEntity entity = OrderDTO.toEntity(order);
        return OrderDTO.fromEntity(service.createOrder(entity));
    }

    @PUT
    public void updateExistingOrder(OrderDTO order) {
        OrderEntity entity = OrderEntity.from(order);
        service.updateOrder(entity);
    }
    
}