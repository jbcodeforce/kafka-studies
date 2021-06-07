package org.acme;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import io.apicurio.registry.client.RegistryRestClient;
import io.apicurio.registry.client.RegistryRestClientFactory;
import io.apicurio.registry.rest.beans.ArtifactMetaData;
import io.apicurio.registry.rest.beans.IfExistsType;
import io.apicurio.registry.types.ArtifactType;
import io.apicurio.registry.utils.serde.AbstractKafkaSerDe;
import io.apicurio.registry.utils.serde.AbstractKafkaSerializer;
import io.apicurio.registry.utils.serde.AvroKafkaDeserializer;
import io.apicurio.registry.utils.serde.AvroKafkaSerializer;
import io.apicurio.registry.utils.serde.strategy.FindBySchemaIdStrategy;
import io.apicurio.registry.utils.serde.strategy.SimpleTopicIdStrategy;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class SimplestAvroExample implements QuarkusApplication {

   private static final Logger LOGGER = Logger.getLogger("SimplestAvroExample");
   private static String TOPIC_NAME = "test";
   private static String BOOTSTRAP_SERVERS = "localhost:9092";
   private static String REGISTRY_URL = "http://localhost:8090/api";
   private static String SCHEMA_DEFINITION = "{\"type\":\"record\",\"name\":\"BasicMessage\",\"fields\":[{\"name\":\"Message\",\"type\":\"string\"},{\"name\":\"Time\",\"type\":\"long\"}]}";


   @Override
   public int run(String... args) throws Exception {
      System.out.println("SimplestAvroExample started");
      createSchemaInServiceRegistry(TOPIC_NAME,SCHEMA_DEFINITION);
      generateEvents();
      consumeEvents();
      return 0;
   }

   private void generateEvents() throws InterruptedException {
      LOGGER.info("=====> Create the Kafka producer");
      Properties props = buildProducerProperties();
      Producer<Object, Object> producer = new KafkaProducer<>(props);
      Schema schema = new Schema.Parser().parse(SCHEMA_DEFINITION);
      GenericRecord record = new GenericData.Record(schema);
      Date now = new Date();
      String message = "Hello to you!";
      record.put("Message", message);
      record.put("Time", now.getTime());
      LOGGER.info("=====> Sending message " + message + " to topic " + TOPIC_NAME);
      try {
         ProducerRecord<Object, Object> producedRecord = new ProducerRecord<>(TOPIC_NAME, "key-1", record);
         producer.send(producedRecord);
   
         Thread.sleep(3000);
      } catch (javax.ws.rs.WebApplicationException e) {
         // strange execption
      } finally {
         producer.flush();
         producer.close();
      }
      
      
   }

   private  void consumeEvents() {
      Properties props = buildConsumerProperties();
      KafkaConsumer<Long, GenericRecord> consumer = new KafkaConsumer<>(props);

      // Subscribe to the topic
      LOGGER.info("=====> Subscribing to topic: " + TOPIC_NAME);
      consumer.subscribe(Collections.singletonList(TOPIC_NAME));

      // Consume messages!!
      LOGGER.info("=====> Consuming messages...");
      try {
          while (Boolean.TRUE) {
              final ConsumerRecords<Long, GenericRecord> records = consumer.poll(Duration.ofSeconds(1));
              if (records.count() == 0) {
                  // Do nothing - no messages waiting.
              } else records.forEach(record -> {
                  LOGGER.info("=====> CONSUMED: " + record.topic() 
                        + " " + record.partition()
                        + " " + record.offset()
                        + " " + record.value());
              });
          }
      } finally {
          consumer.close();
      }
   }

   private  Properties buildCommonProperties() {
      Properties props = new Properties();
      // Configure Service Registry location and ID strategies
      props.putIfAbsent(AbstractKafkaSerDe.REGISTRY_URL_CONFIG_PARAM, REGISTRY_URL);
      props.putIfAbsent(AbstractKafkaSerializer.REGISTRY_ARTIFACT_ID_STRATEGY_CONFIG_PARAM,
            SimpleTopicIdStrategy.class.getName());
      props.putIfAbsent(AbstractKafkaSerializer.REGISTRY_GLOBAL_ID_STRATEGY_CONFIG_PARAM,
            FindBySchemaIdStrategy.class.getName());
      return props;
   }

   private  Properties buildProducerProperties() {
      Properties props = buildCommonProperties();
      props.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
      props.putIfAbsent(ProducerConfig.CLIENT_ID_CONFIG, "Producer-" + TOPIC_NAME);
      props.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
      props.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroKafkaSerializer.class.getName());
      return props;
   }

   private  Properties buildConsumerProperties() {
      Properties props = buildCommonProperties();
      props.putIfAbsent(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
      props.putIfAbsent(ConsumerConfig.GROUP_ID_CONFIG, "Consumer-" + TOPIC_NAME);
      props.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
      props.putIfAbsent(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
      props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroKafkaDeserializer.class.getName());
    
      return props;
   }

   private static void createSchemaInServiceRegistry(String artifactId, String schema) throws Exception {

      LOGGER.info("---------------------------------------------------------");
      LOGGER.info("=====> Creating artifact in the registry for Avro Schema with ID: " + artifactId);
      try {
          RegistryRestClient client = RegistryRestClientFactory.create(REGISTRY_URL);
          ByteArrayInputStream content = new ByteArrayInputStream(schema.getBytes(StandardCharsets.UTF_8));
          ArtifactMetaData metaData = client.createArtifact(artifactId, ArtifactType.AVRO, IfExistsType.RETURN, content);
          LOGGER.info("=====> Successfully created Avro Schema artifact in Service Registry: " + metaData);
          LOGGER.info("---------------------------------------------------------");
      } catch (Exception t) {
          throw t;
      }
  }
  
}