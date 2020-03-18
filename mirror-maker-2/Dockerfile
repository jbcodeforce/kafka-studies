FROM strimzi/kafka:latest-kafka-2.4.0
USER root
WORKDIR /home
COPY truststore.jks /home
COPY local-cluster/kafka-to-es.properties /home
COPY local-cluster/config.yaml /home
COPY local-cluster/jmx_prometheus_javaagent-0.12.0.jar /home
RUN export LOG_DIR=/tmp/logs
CMD /opt/kafka/bin/connect-mirror-maker.sh -javaagent:/home/jmx_prometheus_javaagent-0.12.0.jar=8080:/home/config.yaml  /home/kafka-to-es.properties

