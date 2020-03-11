#!/usr/bin/env bash
if [[ $# -ne 1 ]];then
    topic=products
else
    topic=$1
fi
source ../setenv.sh

/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server $KAFKA_SOURCE_BROKERS  --topic $topic  --from-beginning 