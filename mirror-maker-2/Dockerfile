FROM strimzi/kafka:latest-kafka-2.4.0
USER root
WORKDIR /home
RUN export LOG_DIR=/tmp/logs
CMD /opt/kafka/bin/connect-mirror-maker.sh /home/kafka-to-es-mm2.properties

