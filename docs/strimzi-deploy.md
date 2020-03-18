# Strimzi Kafka deployment on Openshift or Kubernetes

[Strimzi](https://strimzi.io/) uses the Cluster Operator to deploy and manage Kafka (including Zookeeper) and Kafka Connect clusters. When the Strimzi Cluster Operator is up, it starts to watch for certain OpenShift or Kubernetes resources containing the desired Kafka and/or Kafka Connect cluster configuration. The base of strimzi is to define a set of kubernetes operators and custom resource definitions for the different elements of Kafka. 

![](images/strimzi.png)

We recommend to go over the product [overview page](https://strimzi.io/docs/overview/master/).

## Concept summary

The Cluster Operator is a pod used to deploys and manages Apache Kafka clusters, Kafka Connect, Kafka MirrorMaker (1 and 2), Kafka Bridge, Kafka Exporter, and the Entity Operator. When deployed the following commands goes to the Cluster operator:

```shell
# Get the current cluster list
oc get kafka
# get the list of topic
oc get kafkatopics
```

Example of topic can be seen in [section below](#create-a-topic).

Kafka User are not saved part of kafka cluster but they are managed in kubernetes. For example the user credentials are saved as secret.

CRDs act as configuration instructions to describe the custom resources in a Kubernetes cluster, and are provided with Strimzi for each Kafka component used in a deployment. 

## Deployment

The deployment is done in two phases:

* Deploy the Custom Resource Definitions (CRDs), which act as specifications of the custom resource to deploy.
* Deploy one to many instance of those CRDs

In CR yaml file the `kind` attribute specifies the CRD to conform to.

Each CRD has a common configuration like bootstrap servers, CPU resources, logging, healthchecks...

The next steps are defining how to deploy a Kafka Cluster.

### Create a namespace or openshift project

```shell
kubectl create namespace jb-kafka-strimzi

oc create project jb-kafka-strimzi
```

### Download the strimzi artefacts

For the last release see [this github page](https://github.com/strimzi/strimzi-kafka-operator/releases). Then modify the Role binding yaml files with the namespace set in previous step.

```shell
sed -i '' 's/namespace: .*/namespace: kafka-strimzi/' install/cluster-operator/*RoleBinding*.yaml
```

### Deploy the Custom Resource Definitions for kafka

Custom resource definitions are defined within the kubernetes cluster. The following command  

```shell
oc apply -f install/cluster-operator/ 
```

This should create the following resource definitions:

| Names | Resource | Command |
| :---: | :---: | :---: |
| strimzi-cluster-operator | Service account | oc get sa |
| strimzi-cluster-operator-entity-operator-delegation, strimzi-cluster-operator, strimzi-cluster-operator-topic-operator-delegation | Role binding | oc get rolebinding |
| strimzi-cluster-operator-global, strimzi-cluster-operator-namespaced, strimzi-entity-operator, strimzi-kafka-broker, strimzi-topic-operator | Cluster Role | oc get clusterrole |
| strimzi-cluster-operator, strimzi-cluster-operator-kafka-broker-delegation | Cluster Role Binding | oc get clusterrolebinding |
| kafkabridges, kafkaconnectors, kafkaconnects, kafkamirrormaker2s kafka, kafkatopics, kafkausers | Custom Resource Definition | oc get customresourcedefinition |

## Deploy instances

### Deploy Kafka cluster

The CRD for kafka cluster resource is [here](https://github.com/strimzi/strimzi-kafka-operator/blob/2d35bfcd99295bef8ee98de9d8b3c86cb33e5842/install/cluster-operator/040-Crd-kafka.yaml).

Change the name of the cluster in one the yaml in the `examples/kafka` folder or use the `strimzi/kafka-cluster.yml` file in this project. For productionm we need to use persistence for the kafka log, ingress or load balancer external listener and rack awareness policies.

Using non presistence:

```shell
oc apply -f examples/kafka/kafka-ephemeral.yaml -n jb-kafka-strimzi
oc get kafka
# NAME         DESIRED KAFKA REPLICAS   DESIRED ZK REPLICAS
# my-cluster   3                        3
# Or
kubectl  apply -f examples/kafka/kafka-ephemeral.yaml -n jb-kafka-strimzi
```

When looking at the pods running we can see the three kafka and zookeeper nodes as pods, and the entity operator pod.

```shell
$ oc get pods
my-cluster-entity-operator-645fdbc4cb-m29nk   3/3       Running     0          18d
my-cluster-kafka-0                            2/2       Running     0          3d
my-cluster-kafka-1                            2/2       Running     0          3d
my-cluster-kafka-2                            2/2       Running     0          3d
my-cluster-zookeeper-0                        2/2       Running     0          3d
my-cluster-zookeeper-1                        2/2       Running     0          3d
my-cluster-zookeeper-2                        2/2       Running     0          3d
strimzi-cluster-operator-58cbbcb7d-bcqhm      1/1       Running     2         18d
strimzi-topic-operator-564654cb86-nbt58       1/1       Running     1         18d
```

Using persistence:

```shell
oc apply -f strimzi/kafka-cluster.yaml
```

### Topic Operator

The role of the `Topic Operator` is to keep a set of KafkaTopic OpenShift or Kubernetes resources describing Kafka topics in-sync with corresponding Kafka topics.

```shell
oc apply -f install/topic-operator/ -n jb-kafka-strimzi
```

This will add the following:

| Names | Resource | Command |
| :---: | :---: | :---: |
| strimzi-topic-operator | Service account | oc get sa |
| strimzi-topic-operator| Role binding | oc get rolebinding |
| kafkatopics | Custom Resource Definition | oc get customresourcedefinition |

### Create a topic

Edit a yaml file like the following:

```yaml
apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaTopic
metadata:
  name: test
  labels:
    strimzi.io/cluster: my-cluster
spec:
  partitions: 1
  replicas: 3
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824
```

```shell
oc apply -f test.yaml -n jb-kafka-strimzi

oc get kafkatopics
```

This creates a topic `test` in your kafka cluster.

## Test with producer and consumer pods

Use kafka-consumer and producer tools from Kafka distribution. Verify within Dockerhub under the Strimzi account to get the lastest image tag (below we use -2.4.0 tag).

```shell
# Start a consumer on test topic

oc run kafka-consumer -ti --image=strimzi/kafka:latest-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic test --from-beginning
# Start a text producer
oc run kafka-producer -ti --image=strimzi/kafka:latest-kafka-2.4.0  --rm=true --restart=Never -- bin/kafka-console-producer.sh --broker-list my-cluster-kafka-bootstrap:9092 --topic test
# enter text
```

If you want to use the strimzi kafka docker image to run the above scripts locally but remotely connect to a kafka cluster you need multiple things to happen:

* Be sure the kafka yaml file include the external route stamza:

```yaml
spec:
  kafka:
    version: 2.4.0
    replicas: 3
    listeners:
      plain: {}
      tls: {}
      external:
        type: route
```

* Get the host ip address from the Route resource

```shell
oc get routes my-cluster-kafka-bootstrap -o=jsonpath='{.status.ingress[0].host}{"\n"}'
```

* Get the TLS certificate from the broker

```shell
oc get secrets
oc extract secret/my-cluster-cluster-ca-cert --keys=ca.crt --to=- > ca.crt
# transform it fo java truststore
keytool -import -trustcacerts -alias root -file ca.crt -keystore truststore.jks -storepass password -noprompt
```

*The alias is used to access keystore entries (key and trusted certificate entries).*

* Start the docker container by mounting the local folder with the truststore.jks to the `/home`

```shell
docker run -ti -v $(pwd):/home strimzi/kafka:latest-kafka-2.4.0  bash
# inside the container uses the consumer tool
bash-4.2$ cd /opt/kafka/bin
bash-4.2$ ./kafka-console-consumer.sh --bootstrap-server  my-cluster-kafka-bootstrap-jb-kafka-strimzi.gse-eda-demos-fa9ee67c9ab6a7791435450358e564cc-0001.us-east.containers.appdomain.cloud:443 --consumer-property security.protocol=SSL --consumer-property ssl.truststore.password=password --consumer-property ssl.truststore.location=/home/truststore.jks --topic test --from-beginning
```

* For a producer the approach is the same but using the producer properties:

```
./kafka-console-producer.sh --broker-list  my-cluster-kafka-bootstrap-jb-kafka-strimzi.gse-eda-demos-fa9ee67c9ab6a7791435450358e564cc-0001.us-east.containers.appdomain.cloud:443 --producer-property security.protocol=SSL --producer-property ssl.truststore.password=password --producer-property ssl.truststore.location=/home/truststore.jks --topic test   
```

Those properties can be in file

```shell
bootstrap.servers=my-cluster-kafka-bootstrap-jb-kafka-strimzi.gse-eda-demos-fa9ee67c9ab6a7791435450358e564cc-0001.us-east.containers.appdomain.cloud
security.protocol=SSL
ssl.truststore.password=password
ssl.truststore.location=/home/truststore.jks
```

and then use the following parameters in the command line:

```shell
./kafka-console-producer.sh --broker-list my-cluster-kafka-bootstrap-jb-kafka-strimzi.gse-eda-demos-fa9ee67c9ab6a7791435450358e564cc-0001.us-east.containers.appdomain.cloud:443 --producer.config /home/strimzi.properties --topic test

./kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap-jb-kafka-strimzi.gse-eda-demos-fa9ee67c9ab6a7791435450358e564cc-0001.us-east.containers.appdomain.cloud:443  --topic test  --consumer.config /home/strimzi.properties --from-beginning 
```

### Deploying Kafka Connect cluster

Adding a Kafka Connect cluster on top of an existing Kafka cluster using Strimzi operators is simple.

### Deploying Mirror Maker 2.0

In this section we address another approach to, deploy a Kafka Connect cluster with Mirror Maker 2.0 connectors but without any local Kafka Cluster. The appraoch will be to use Event Streams on Cloud as backend Kafka cluster but use Mirror Maker for replication. The steps could 

