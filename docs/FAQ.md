# Frequently asked questions

## How to support exactly once delivery?

See the section in the producer implementation considerations [note]().

Also it is important to note that the Kafka Stream API supports exactly once semantics with the config: `processing.guarantee=exactly_once`. Each task within a read-process-write flow may fail so this setting is important to be sure the right answer is delivered, even in case of task failure, and the process is executed exactly once.

## Why does kafka use zookeeper?

Kafka as a distributed system using cluster, it needs to keep cluster states, sharing configuration like topic, assess which node is still alive within the cluster, support registrering new node added to the cluster, being able to support dynamic restart. Zookeeper is an orchestrator for distributed system, it maintains kafka cluster integrity, select broker leader... 

## Retention time for topic what does it mean?

The message sent to a cluster is kept for a max period of time or until a max size is reached. Those topic properties are: `retention.ms` and `retention.bytes`. Messages stay in the log even if they are consumed. The oldest messages are marked for deletion or compaction depending of the cleanup policy (delete or compact) set to `cleanup.policy` parameter.

See the kafka documentation on [topic configuration parameters](https://kafka.apache.org/documentation/#topicconfigs). 

Here is a command to create a topic with specific retention properties:

```
bin/kafka-configs --zookeeper XX.XX.XX.XX:2181 --entity-type topics --entity-name orders --alter --add-config  retention.ms=55000 --add-config  retention.byte=100000
```

But there is also the `offsets.retention.minutes` property, set at the cluster level to control when the offset information will be deleted. It is defaulted to 1 day, but the max possible value is 7 days. This is to avoid keeping too much information in the broker memory and avoid to miss data when consumers run not continuously. So consumers need to commit their offset. If the consumer settings define: `auto.offset.reset=earliest`, the consumer will reprocess all the events each time it restarts, (or skips to the latest if set to `latest`). When using `latest`, if the consumers are offline for more than the offsets retention time window, they will lose events.

## What are the topic characteristics I need to define during requirements?

This is a requirement gathering related question, to understand what need to be done for configuration topic configuration but also consumer and producer configuration, as well as retention strategy.

* Number of brokers in the cluster
* fire or forget or persist data for which amount of time
* Need for HA, set replicas to number of broker or at least the value of 3
* Type of data to transport
* Schema management to control change to the payload definition
* volume per day
* Accept snapshot
* Need to do ge replication to other kafka cluster
* Network filesystem used on the target kubernetes cluster and current storage class

## What are the impacts of having not enough resource for kafka?

The table in this [Event Streams product documentation](https://ibm.github.io/event-streams/installing/prerequisites/#helm-resource-requirements) illustrates the resource requirements for a getting started cluster. When resources start to be at stress, then Kafka communication to ZooKeeper and/or other Kafka brokers can suffer resulting in out-of-sync partitions and container restarts perpetuating the issue. Resource constraints is one of the first things we consider when diagnosing ES issues.

## What does out-of-synch partition mean and occur?

With partition leader and replication to the followers, the number of in-synch replicas is at least the number of expected replicas. For example for a replicas = 3 the in-synch is set to 2, and it represents the minimum number of replicas that must acknowledge a write for the write to be considered successful. The record is considered “committed” when all ISRs for partition wrote to their log. Only committed records are readable from consumer.

So out-of-synch will happen if the followers are not able to send their acknowledge to the replica leader.

## Differences between Akka and Kafka?

[Akka](https://akka.io/) is a open source toolkit for Scala or Java to simplify multithreading programming and makes application more reactive by adopting an asynchronous mechanism to access to io: database or HTTP request. To support asynchronous communication between 'actors', it uses messaging, internal to the JVM. 
Kafka is part of the architecture, while Akka is an implementation choice for one of the component of the business application deployed inside the architecture.

[vert.x](https://vertx.io/) is another open source implementation of such internal messaging mechanism but supporting more language:  Java, Groovy, Ruby, JavaScript, Ceylon, Scala, and Kotlin.


## Run Kafka Test Container with TopologyTestDriver

Topology Test Driver is used without kafka, so there is no real need to use test container. 

## Event streams resource requirements 

See the [detailed tables](https://ibm.github.io/event-streams/installing/prerequisites/#helm-resource-requirements) in the product documentation.

## Security setting

On Kubernetes, Kafka can be configured with external and internal URLs. With Strimzi internal URLs are using TLS or Plain authentication, then TLS for encryption. 

If no authentication property is specified then the listener does not authenticate clients which connect through that listener. The listener will accept all connections without authentication.


* Mutual TLS authentication for internal communication looks like:

```yaml
- name: tls
    port: 9093
    type: internal
    tls: true
    authentication:
      type: tls
```

To connect any app (producer, consumer) we need a TLS user like:

```yaml
piVersion: kafka.strimzi.io/v1beta1
kind: KafkaUser
metadata:
  name: tls-user
  labels:
    strimzi.io/cluster: vaccine-kafka
spec:
  authentication:
    type: tls
```

Then the following configurations need to be done for each app. For example in Quarkus app, we need to specify where to find the client certificate (for each Kafka TLS user a secret is created with the certificate (ca.crt) and a user password)

```shell
oc describe secret tls-user
Data
====
ca.crt:         1164 bytes
user.crt:       1009 bytes
user.key:       1704 bytes
user.p12:       2374 bytes
user.password:  12 bytes
```

For Java client we need the following security settings, to specify from which secret to get the keystore password and certificate. The certificate will be mounted to `/deployments/certs/user`. 

```shell
%prod.kafka.security.protocol=SSL
%prod.kafka.ssl.keystore.location=/deployments/certs/user/user.p12
%prod.kafka.ssl.keystore.type=PKCS12
quarkus.openshift.env.mapping.KAFKA_SSL_KEYSTORE_PASSWORD.from-secret=${KAFKA_USER:tls-user}
quarkus.openshift.env.mapping.KAFKA_SSL_KEYSTORE_PASSWORD.with-key=user.password
quarkus.openshift.mounts.user-cert.path=/deployments/certs/user
quarkus.openshift.secret-volumes.user-cert.secret-name=${KAFKA_USER:tls-user}
# To validate server side certificate we will mount it too with the following declaration
quarkus.openshift.env.mapping.KAFKA_SSL_TRUSTSTORE_PASSWORD.from-secret=${KAFKA_CA_CERT_NAME:kafka-cluster-ca-cert}
quarkus.openshift.env.mapping.KAFKA_SSL_TRUSTSTORE_PASSWORD.with-key=ca.password
quarkus.openshift.mounts.kafka-cert.path=/deployments/certs/server
quarkus.openshift.secret-volumes.kafka-cert.secret-name=${KAFKA_CA_CERT_NAME:kafka-cluster-ca-cert}
```

For the server side certificate, it will be in a truststore, which is mounted to  `/deployments/certs/server` and from a secret (this secret is created at the cluster level).

Also because we also use TLS for encryption we need:

```
%prod.kafka.ssl.protocol=TLSv1.2
```

Mutual TLS authentication is always used for the communication between Kafka brokers and ZooKeeper pods. For mutual, or two-way, authentication, both the server and the client present certificates.

* SCRAM: (Salted Challenge Response Authentication Mechanism) is an authentication protocol that can establish mutual authentication using passwords. Strimzi can configure Kafka to use SASL (Simple Authentication and Security Layer) SCRAM-SHA-512 to provide authentication on both unencrypted and encrypted client connections.
    * The listener declaration:
    ```yaml
    - name: external
    port: 9094
    type: route
    tls: true
    authentication:
      type: scram-sha-512
    ```
    * Need a scram-user:

    ```yaml
    apiVersion: kafka.strimzi.io/v1beta1
    kind: KafkaUser
    metadata:
    name: scram-user
    labels:
        strimzi.io/cluster: vaccine-kafka
    spec:
    authentication:
        type: scram-sha-512
    ```

Then the app properties need:

```shell
security.protocol=SASL_SSL
%prod.quarkus.openshift.env.mapping.KAFKA_SSL_TRUSTSTORE_PASSWORD.from-secret=${KAFKA_CA_CERT_NAME:kafka-cluster-ca-cert}
%prod.quarkus.openshift.env.mapping.KAFKA_SSL_TRUSTSTORE_PASSWORD.with-key=ca.password
%prod.quarkus.openshift.env.mapping.KAFKA_SCRAM_PWD.from-secret=${KAFKA_USER:scram-user}
%prod.quarkus.openshift.env.mapping.KAFKA_SCRAM_PWD.with-key=password
%prod.quarkus.openshift.mounts.kafka-cert.path=/deployments/certs/server
%prod.quarkus.openshift.secret-volumes.kafka-cert.secret-name=${KAFKA_CA_CERT_NAME:kafka-cluster-ca-cert}
```

## Verify consumer connection

Here is an example of TLS authentication for Event streams

```
ConsumerConfig values: 
	bootstrap.servers = [eda-dev-kafka-bootstrap.eventstreams.svc:9093]
	check.crcs = true
	client.dns.lookup = default
	client.id = cold-chain-agent-c2c11228-d876-4db2-a16a-ea7826e358d2-StreamThread-1-restore-consumer
	client.rack = 
	connections.max.idle.ms = 540000
	default.api.timeout.ms = 60000
	enable.auto.commit = false
	exclude.internal.topics = true
	fetch.max.bytes = 52428800
	fetch.max.wait.ms = 500
	fetch.min.bytes = 1
	group.id = null
	group.instance.id = null
	heartbeat.interval.ms = 3000
	interceptor.classes = []
	internal.leave.group.on.close = false
	isolation.level = read_uncommitted
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	sasl.client.callback.handler.class = null
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.mechanism = GSSAPI
	security.protocol = SSL
	security.providers = null
	send.buffer.bytes = 131072
	session.timeout.ms = 10000
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2]
	ssl.endpoint.identification.algorithm = https
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = /deployments/certs/user/user.p12
	ssl.keystore.password = [hidden]
	ssl.keystore.type = PKCS12
	ssl.protocol = TLSv1.2
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = /deployments/certs/server/ca.p12
	ssl.truststore.password = [hidden]
	ssl.truststore.type = PKCS12
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
```

## Kafdrop

For Kafdrop configuration see the kafka properties and `startKafDrop.sh` in scripts folder.

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

## Some error and solution

### ClassCastException: ibm.eda.demo.app.infrastructure.events.OrderEvent incompatible with java.lang.String

We need to have a special serializer for the bean. Quarkus has some [serialization in Json](https://github.com/quarkusio/quarkus/blob/master/extensions/kafka-client/runtime/src/main/java/io/quarkus/kafka/client/serialization/JsonbSerde.java).



## Other FAQs

[IBM Event streams on Cloud FAQ](https://cloud.ibm.com/docs/services/EventStreams?topic=eventstreams-faqs) 

[FAQ from Confluent](https://cwiki.apache.org/confluence/display/KAFKA/FAQ#FAQ-HowareKafkabrokersdependonZookeeper?)