package jb.vertx.kafka.producer;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class KafkaConfig {

    @Inject
    @ConfigProperty(name="topic",defaultValue = "vaccine-orders")
    public  String topicName;

    public  Map<String,String> setup(){
        Map<String, String> props = new HashMap<String,String>();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                  ConfigProvider.getConfig().getValue("bootstrap.servers",String.class));
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        props.put(SaslConfigs.SASL_JAAS_CONFIG,
                  ConfigProvider.getConfig().getValue("sasl.jaas.config",String.class));
        
        props.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
       
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, 
            ConfigProvider.getConfig().getValue("ssl.truststore.location",String.class));
        
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  
            ConfigProvider.getConfig().getValue("ssl.truststore.password",String.class));
       
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("ssl.endpoint.identification.algorithm","HTTPS");
        return props;
    }

    public String getTopic(){
        return topicName;
    }
}