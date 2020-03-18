FROM strimzi/kafka:latest-kafka-2.4.0
USER root
WORKDIR /home
COPY truststore.jks /home
COPY local-cluster/kafka-to-es.properties /home
RUN export LOG_DIR=/tmp/logs
CMD /opt/kafka/bin/connect-mirror-maker.sh /home/kafka-to-es.properties

