package jb.vertx.kafka.producer;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.producer.KafkaProducer;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;

@Path("/orders")
public class OrderResource {
    static final Logger logger = Logger.getLogger(OrderResource.class);
    @Inject
    Vertx vertx;
    
    @Inject
    KafkaConfig config;

    Jsonb jsonb = JsonbBuilder.create();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Uni<VaccineOrder> getOrderById(@PathParam String id) {
        VaccineOrder o = new VaccineOrder();
        o.deliveryLocation="Paris/France";
        o.priority=1;
        o.quantity=2000;
        o.status = OrderStatus.OPEN;
        o.id=id;
        return Uni.createFrom().item(o);
        
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public  Uni<VaccineOrder> saveNewVaccineOrder(VaccineOrder o){
        o.id=UUID.randomUUID().toString();
        o.status = OrderStatus.OPEN;
        processOrder(o);
        return Uni.createFrom().item(o);
    }

    public OrderResource() {
    }

    public void processOrder(VaccineOrder o){
        Map<String, String> props = config.setup();
       props.put(ProducerConfig.RETRIES_CONFIG, "0");
       props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");
        KafkaProducer<String, String> kafkaProducer = KafkaProducer.create(vertx, props);
        kafkaProducer.exceptionHandler(err -> logger.debug("Kafka error: {}", err));
       
        String payload = jsonb.toJson(o);
        KafkaProducerRecord<String, String> record = KafkaProducerRecord
                .create(config.getTopic(), o.id, payload);
        logger.info("Producing record to topic " + config.getTopic() + " message: " + payload);
    
        kafkaProducer
          .sendAndForget(record);
        kafkaProducer.close();
    }
}