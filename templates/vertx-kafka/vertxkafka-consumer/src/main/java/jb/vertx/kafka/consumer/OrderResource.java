package jb.vertx.kafka.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;

@Path("/orders")
@ApplicationScoped
public class OrderResource {
    static final Logger logger = Logger.getLogger(OrderResource.class);
        
    @Inject
    Vertx vertx;
    
    @Inject
    KafkaConfig config;

    KafkaConsumer<String, String> kafkaConsumer;
    List<VaccineOrder> receivedOrders;
       
    void onStart(@Observes StartupEvent ev) {               
        logger.info("The application is starting...");
        kafkaConsumer = KafkaConsumer.create(vertx, config.setup());
        receivedOrders = new ArrayList<VaccineOrder>();
        kafkaConsumer.handler(record -> {       
            VaccineOrder o = jsonb.fromJson(record.value(),VaccineOrder.class);
            System.out.println("Received record " + record.value() + " offset " + record.offset());
            receivedOrders.add(o);
            kafkaConsumer.commit();
          });
        kafkaConsumer.subscribe(config.getTopic())
          .onItem().invoke(p -> System.out.println( config.getTopic() +" subcribed"));
        
    }

    void onStop(@Observes ShutdownEvent ev) {               
        logger.info("The application is stopping...");
    }


    Jsonb jsonb = JsonbBuilder.create();
    
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<VaccineOrder> getOrders(){
        
        return Multi.createFrom().items(receivedOrders.stream());
    }
}