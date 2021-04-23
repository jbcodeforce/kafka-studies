# Frequently asked questions

Moved to [eda faq](https://ibm-cloud-architecture.github.io/refarch-eda/technology/faq/)

## How internal and external listerner work

See [this article](https://rmoff.net/2018/08/02/kafka-listeners-explained/) to understand thee listener configuration. Here is a config to be used in docker container:

```
 KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:29092,EXTERNAL://localhost:9092
 KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
 KAFKA_LISTENERS: EXTERNAL://0.0.0.0:9092,INTERNAL://kafka:29092
 KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
 KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
```


## Some error and solution

### ClassCastException: ibm.eda.demo.app.infrastructure.events.OrderEvent incompatible with java.lang.String

We need to have a special serializer for the bean. Quarkus has some [serialization in Json](https://github.com/quarkusio/quarkus/blob/master/extensions/kafka-client/runtime/src/main/java/io/quarkus/kafka/client/serialization/JsonbSerde.java).



## Other FAQs

[IBM Event streams on Cloud FAQ](https://cloud.ibm.com/docs/services/EventStreams?topic=eventstreams-faqs) 

[FAQ from Confluent](https://cwiki.apache.org/confluence/display/KAFKA/FAQ#FAQ-HowareKafkabrokersdependonZookeeper?)