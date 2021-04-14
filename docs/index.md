# Apache Kafka Studies

This repository regroups a set of personal studies and quick summaries on Kafka. Most of the curated content is  defined in [the Kafka overview article](https://ibm-cloud-architecture.github.io/refarch-eda/technology/kafka-overview/), and [the producer and consumer one](https://ibm-cloud-architecture.github.io/refarch-eda/technology/kafka-producers-consumers/).


## Kafka local

The docker compose in this repo, starts one zookeeper and one Kafka broker locally using last Strimzi release, and one Apicurio for schema registry.

In the docker compose I found the following issue with the listeners: if set with localhost, then the kafka broker is accessible from app outside of the docker network, so a quarkus app running with `quarkus:dev` will connect. But a container in the same network needs to access kafka node, so the listener needs to declare two listeners

```yaml
 ports:
      - "29092:9092"
 environment:
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,EXTERNAL://localhost:29092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT
    KAFKA_LISTENERS: EXTERNAL://0.0.0.0:29092,PLAINTEXT://0.0.0.0:9092
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
```

To start [kafkacat](https://hub.docker.com/r/edenhill/kafkacat) and [kafkacat doc to access sample consumer - producer](https://github.com/edenhill/kafkacat#examples)

```shell
docker run -it --network=host edenhill/kafkacat -b kafka1:9092 -L
```

## Using Apicurio

Once started define a schema in json and upload it to the api: http://apicurio:8080/api. Here is an example of command:

Schema:
```
{   
    "namespace": "ibm.gse.eda.vaccine.orderoptimizer",
    "doc": "Avro data schema for Reefer events",
    "type":"record",
    "name":"Reefer",
    "fields":[
            {
                "name": "reefer_id",
                "type": "string",
                "doc": "Reefer container ID"
            },
            {
                "name": "status",
                "type": "string",
                "doc": "Reefer Container ID status. Could be an Enum"
            },
            {
                "name": "location",
                "type": "string",
                "doc": "Reefer container location"
            },
            {
                "name": "date_available",
                "type": "string",
                "doc": "Date when the inventory will be available"
            }
     ]
}
```

Upload it to schema registry

```shell 
curl -X POST -H "Content-type: application/json; artifactType=AVRO" \
   -H "X-Registry-ArtifactId: vaccine.reefers-value" \
   --data @${scriptDir}/../data/avro/schemas/reefer.avsc http://localhost:8080/api/artifacts
```


## This repository content

Outside of my personal notes, some folders include running app:

* [python-kafka](https://github.com/jbcodeforce/kafka-studies/tree/master/python-kafka) for simple reusable code for event consumer and producer with python.
* [Kafka Vertx starter code](https://github.com/jbcodeforce/kafka-studies/tree/master/kafka-java-vertx-starter-1.0.0) from the event streams team, within one app to test a deployed event stream deployment
* [vertx consumer and producer](https://github.com/jbcodeforce/kafka-studies/tree/master/vertx-kafka) as separate quarkus apps.


## Source of information

* [Our event driven reference architecture content](https://ibm-cloud-architecture.github.io/refarch-eda/)
* [Container shipment solution](https://ibm-cloud-architecture.github.io/refarch-kc/)
* [Start by reading Kafka introduction - a must read!](https://Kafka.apache.org/intro/)
* [IBM Event Streams Product Documentation](https://ibm.github.io/event-streams)

* [Another introduction from Confluent, one of the main contributors of the open source.](http://www.confluent.io/blog/introducing-Kafka-streams-stream-processing-made-simple)

* [Planning event streams installation](https://ibm.github.io/event-streams/installing/planning/)
* [Develop Stream Application using Kafka](https://Kafka.apache.org/15/documentation/streams/)
* [Tutorial on access control, user authentication and authorization from IBM.](https://developer.ibm.com/tutorials/kafka-authn-authz/)
* [Kafka on Kubernetes using stateful sets](https://github.com/kubernetes/contrib/tree/master/statefulsets/Kafka)
* [IBM Developer article - learn kafka](https://developer.ibm.com/messaging/event-streams/docs/learn-about-Kafka/)
* [Using Kafka Connect to connect to enterprise MQ systems - Andrew Schofield](https://medium.com/@andrew_schofield/using-kafka-connect-to-connect-to-enterprise-mq-systems-5674d53fe55e)
* [Does Apache Kafka do ACID transactions? - Andrew Schofield](https://medium.com/@andrew_schofield/does-apache-kafka-do-acid-transactions-647b207f3d0e)
* [Spark and Kafka with direct stream, and persistence considerations and best practices](http://aseigneurin.github.io/2016/05/07/spark-Kafka-achieving-zero-data-loss.html)
* [Example in scala for processing Tweets with Kafka Streams](https://www.madewithtea.com/processing-tweets-with-Kafka-streams.html)

## Kafka programming

* [Producer & Consumer considerations](https://ibm-cloud-architecture.github.io/refarch-eda/technology/kafka-producers-consumers/)
* [Multithreading consumer study note from confluent]()

### Event streams

Product documentation to access event streams.

* Event streams / kafka on OpenShift with TLS connection on external route. We need the following quarkus properties:

 ```shell
 # get environment variables from configmap
 quarkus.openshift.env.configmaps=vaccine-order-ms-cm
 # use docker compose kafka
 %dev.kafka.bootstrap.servers=kafka:9092
 ```

 with matching config map

 ```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: vaccine-order-ms-cm
data:
  KAFKA_BOOTSTRAP_SERVERS: eda-dev-kafka-bootstrap-eventstreams.gse-eda-2021-1-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-east.containers.appdomain.cloud:443
  KAFKA_SSL_PROTOCOL: TLSv1.2
  KAFKA_USER: scram
  KAFKA_SSL_TRUSTSTORE_LOCATION: /deployments/certs/server/ca.p12
  KAFKA_SSL_TRUSTSTORE_TYPE: PKCS12
  SHIPMENT_PLAN_TOPIC: vaccine_shipment_plans
  KAFKA_SASL_MECHANISM: SCRAM-SHA-512
  KAFKA_SECURITY_PROTOCOL: SASL_SSL
 ```

 and secrets

 ```
 ```

See example in project []()

## Kafka Connect with Debezium

## Kafka with Quarkus

Here is a template code for quarkus based Kafka consumer: [quarkus-event-driven-consumer-microservice-template](https://github.com/jbcodeforce/quarkus-event-driven-consumer-microservice-template).

Read this interesting guide with Quarkus and kafka streaming: [Quarkus using Kafka Streams](https://quarkus.io/guides/kafka-streams), which is implemented in the quarkus-reactive-msg producer, aggregator folders.

To generate the starting code for the producer we use the quarkus maven plugin with kafka extension:
`mvn io.quarkus:quarkus-maven-plugin:1.12.2.Final:create -DprojectGroupId=jbcodeforce.kafka.study -DprojectArtifactId=producer -Dextensions="kafka"`

for the aggregator:

`mvn io.quarkus:quarkus-maven-plugin:1.12.2.Final:create -DprojectGroupId=jbcodeforce.kafka.study -DprojectArtifactId=aggregator -Dextensions="kafka-streams,resteasy-jsonb"`

Interesting how to generate reference value to a topic with microprofile reactive messaging. `stations` is a hash:

```java
    @Outgoing("weather-stations")                               
    public Flowable<KafkaRecord<Integer, String>> weatherStations() {
        List<KafkaRecord<Integer, String>> stationsAsJson = stations.stream()
            .map(s -> KafkaRecord.of(
                    s.id,
                    "{ \"id\" : " + s.id +
                    ", \"name\" : \"" + s.name + "\" }"))
            .collect(Collectors.toList());

        return Flowable.fromIterable(stationsAsJson);
    };
```

Channels are mapped to Kafka topics using the Quarkus configuration file `application.properties`.

To build and run:

```shell
# under producer folder
docker build -f src/main/docker/Dockerfile.jvm -t quarkstream/producer-jvm .
# under aggregator folder
docker build -f src/main/docker/Dockerfile.jvm -t quarkstream/aggregator-jvm .
# Run under quarkus-reactive-msg
docker-compose up
# Run kafkacat
docker run --tty --rm -i --network kafkanet debezium/tooling:1.0
$ kafkacat -b kafka1:9092 -C -o beginning -q -t temperatures-aggregated
```

## Kafka with Apache Camel 3.0