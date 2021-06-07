cd $(dirname $0)

PROP_FILE=kafka.properties 

if [[ $# -eq 1 ]]
then
     PROP_FILE=$1
fi

docker run -ti --rm -p 9000:9000 \
     -v $(pwd)/:/home \
    -e KAFKA_BROKERCONNECT=$KAFKA_BROKERS \
    -e KAFKA_PROPERTIES=$(cat ${PROP_FILE} | base64) \
    -e JVM_OPTS="-Xms32M -Xmx64M" \
    -e SERVER_SERVLET_CONTEXTPATH="/" \
    obsidiandynamics/kafdrop
