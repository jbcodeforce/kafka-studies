# Apache Kafka Studies

This repository regroups a set of personal studies and quick summary on Kafka. This is some development note to keep in mind for Kafka development. Most of the content is already defined in [those best practices](https://ibm-cloud-architecture.github.io/refarch-eda/technology/kafka-overview/). 

## Kafka local

The docker compose starts one zookeeper and one kafka broker locally on the `kafkanet` network.

To start [kafkacat](https://hub.docker.com/r/edenhill/kafkacat) and [kafkacat doc to access sample consumer - producer](https://github.com/edenhill/kafkacat#examples)

```shell
docker run -it --network=host edenhill/kafkacat -b kafka1:9092 -L

```

## Repository content

Outside of the documentation and notes, some folder includes running app:

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

To access event streams on private cloud with truststore we need the jks file to be put under `src/main/liberty/config/resources/security` then set the env variable to this path: `export TRUSTSTORE_PATH="resources/security/certs.jks"`

## Kafka Connect with Debezium

## Kafka with Quarkus

` ./mvnw compile quarkus:dev` to start local quarkus app and continuously develop.

Here is a template code for quarkus based Kafka consumer: [quarkus-event-driven-consumer-microservice-template](https://github.com/jbcodeforce/quarkus-event-driven-consumer-microservice-template).

Read this interesting guide with Quarkus and kafka streaming: [Quarkus using Kafka Streams](https://quarkus.io/guides/kafka-streams), which is implemented in the quarkus-reactive-msg producer, aggregator folders.

To generate the starting code for the producer we use the quarkus maven plugin with kafka extension:
`mvn io.quarkus:quarkus-maven-plugin:1.4.1.Final:create -DprojectGroupId=jbcodeforce.kafka.study -DprojectArtifactId=producer -Dextensions="kafka"`

for the aggregator:

`mvn io.quarkus:quarkus-maven-plugin:1.4.1.Final:create -DprojectGroupId=jbcodeforce.kafka.study -DprojectArtifactId=aggregator -Dextensions="kafka-streams,resteasy-jsonb"`

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