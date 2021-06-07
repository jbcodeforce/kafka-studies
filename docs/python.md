# Kafka programming with python

There are a lot of python based code already defined in [this EDA reference project](https://ibm-cloud-architecture.github.io/refarch-kc) and integration tests.

## Basic consumer and producer

In the `python-kafka` folder, I used a very simple setting to run kafka locally with docker compose and python environment.

[KafkaConsumer.py](https://github.com/jbcodeforce/kafka-studies/blob/master/python-kafka/KafkaConsumer.py) is for used to connect to Kafka brokers, which URL is defined in environment variable (KAFKA_BROKERS) and uses the confluent_kafka library.

To run this consumer using local kafka, first start kafka and zookeeper using docker compose:

```shell
cd python-kafka
docker-compose up &
```

```shell
docker rm Env1
./startPythonDocker.sh Env1
root@19d06f7d163e:/home# cd python-kafka/
root@19d06f7d163e:/home/python-kafka# python KafkaConsumer.py
```

The producer code [KafkaProducer.py](https://github.com/jbcodeforce/kafka-studies/blob/master/python-kafka/KafkaProducer.py) is in separate program and run in a second container

```shell
docker rm Env2
./startPythonDocker.sh Env2 5001
root@44e827a5c2cc:/home# cd python-kafka/
root@44e827a5c2cc:/home/python-kafka# python KafkaProducer.py
```

### Using event streams on Cloud

Set the KAFKA_BROKERS to the brokers URL of the event streams instance. The setenv.sh is used for that:

```shell
root@44e827a5c2cc:/home/kafka# source ./setenv.sh IBMCLOUD
echo $KAFKA_BROKERS
python KafkaConsumer.py   
# or 
python KafkaProducer.py
```


## Faust

The other API to integrate with Kafka is the Faust for streamings. To execute the first basic faust agent and producer code use the following: [FaustEasiestApp.py](https://github.com/jbcodeforce/kafka-studies/blob/master/python-kafka/FaustEasiestApp.py)

As previously start a docker python container and then:

```shell
root@44e827a5c2cc:/home# pip install faust & cd python-kafka
root@44e827a5c2cc:/home/python-kafka# faust -A FaustEasiestApp worker -l info
```

