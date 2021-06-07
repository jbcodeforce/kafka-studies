package jb.vertx.kafka.consumer;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "VaccineOrder", description = "Vaccine order to be delivered in a state or a country")
public class VaccineOrder {
    @Schema(required = false, description = "Unique order identifier, will be generated")
    public String id;
    @Schema(required = true, description = "Location for vaccine lots to be delivered")
    public String deliveryLocation;
    @Schema(required = false,
            description = " higher priority order may displace lower priority order plans",
            enumeration = {"0","1","2"},
            defaultValue = "0")
    public int priority = 0;
    @Schema(required = true, description = "Expected quantity of vaccines")
    public int quantity; 
    @Schema(required = false, description = "if not provided, indicating ASAP")
    public LocalDate deliveryDate;
    public OrderStatus status;

    public VaccineOrder(){}

    public VaccineOrder(String idIn, String deliveryLocation, 
            int quantity, int priority, 
            LocalDate delivery, OrderStatus status) {
        this.id = idIn;
        this.deliveryLocation = deliveryLocation;
        this.quantity = quantity;
        this.priority = priority;
        this.deliveryDate = delivery;
        this.status = status;
    }
}