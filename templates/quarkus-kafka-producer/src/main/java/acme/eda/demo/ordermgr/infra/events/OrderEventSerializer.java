package acme.eda.demo.ordermgr.infra.events;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Serializer;

public class OrderEventSerializer implements Serializer<OrderEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderEventSerializer(){}

    @Override public void close() {
    }
  
    @Override public void configure(Map<String, ?> arg0, boolean arg1) {
    }
  
    @Override
    public byte[] serialize(String arg0, OrderEvent data) {
        byte[] retVal = null;
        if (data == null){
            System.out.println("Null received at serializing");
            return null;
        }
        System.out.println("Serializing...");
        try {
            retVal = objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(retVal);
        return retVal;
    }
}