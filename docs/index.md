# Apache Kafka Studies

Under DESTRUCTION



## Using Kafdrop

For Kafdrop configuration see the `kafka.properties` file and `startKafDrop.sh` in the scripts folder.

To get the user password:

```shell
# Get certificate to put in truststore
oc get secret kafka-cluster-ca-cert -o jsonpath='{.data.ca\.p12}' | base64 --decode >./certs/kafka.p12
# Get certificate password
oc get secret kafka-cluster-ca-cert -o jsonpath='{.data.ca\.password}' | base64 --decode 
# Get user passwor from the jaas config
oc get secret scram-user -o jsonpath='{.data.sasl\.jaas\.config}' | base64 --decode && echo
```

Those results are set in the properties:

```properties
ssl.truststore.password=
username="scram-user" password="";
```

## Using Apicurio

Once Apicurio is started, define a schema in json and upload it to the api: http://apicurio:8080/api. Here is an example of command:

Schema:
```
{   
    "namespace": "acme.vaccine.orderoptimizer",
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


## This repository includes

Outside of my personal notes, some folders include running app:

* [python-kafka](https://github.com/jbcodeforce/kafka-studies/tree/master/python-kafka) for simple reusable code for event consumer and producer with python.
* [Kafka Vertx starter code](https://github.com/jbcodeforce/kafka-studies/tree/master/kafka-java-vertx-starter-1.0.0) from the event streams team, within one app to test a deployed event stream deployment
* [vertx consumer and producer](https://github.com/jbcodeforce/kafka-studies/tree/master/vertx-kafka) as separate quarkus apps.


## Source of information

* [My notes on event-driven architecture](https://jbcodeforce.github.io/eda-studies/)
* [Another introduction from Confluent, one of the main contributors of the open source.](http://www.confluent.io/blog/introducing-Kafka-streams-stream-processing-made-simple)

* [Planning IBM event-streams installation](https://ibm.github.io/event-automation/es/installing/planning/)
* [Using Kafka Connect to connect to enterprise MQ systems - Andrew Schofield](https://medium.com/@andrew_schofield/using-kafka-connect-to-connect-to-enterprise-mq-systems-5674d53fe55e)
* [Does Apache Kafka do ACID transactions? - Andrew Schofield](https://medium.com/@andrew_schofield/does-apache-kafka-do-acid-transactions-647b207f3d0e)
* [Spark and Kafka with direct stream, and persistence considerations and best practices](http://aseigneurin.github.io/2016/05/07/spark-Kafka-achieving-zero-data-loss.html)
* [Example in scala for processing Tweets with Kafka Streams](https://www.madewithtea.com/processing-tweets-with-Kafka-streams.html)


### Strimzi

* This is an example of configuration to do a TLS connection on external route to a Strimzi deployed on k8s. 

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
