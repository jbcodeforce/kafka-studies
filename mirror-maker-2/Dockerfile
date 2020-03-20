FROM strimzi/kafka:latest-kafka-2.4.0
USER root
RUN mkdir -p /etc/jmx_exporter
ADD ./local-cluster/jmx_exporter.yaml /etc/jmx_exporter
ADD ./local-cluster/*.jar /usr/local/share/jars/
USER kafka
WORKDIR /home
COPY truststore.jks /home
COPY local-cluster/kafka-to-es.properties /home
ENV LOG_DIR=/tmp/logs

ENV EXTRA_ARGS="-javaagent:/usr/local/share/jars/jmx_prometheus_javaagent-0.12.0.jar=9400:/etc/jmx_exporter/jmx_exporter.yaml "

ENV CONNECT_REST_PORT="80"
ENV CONNECT_PLUGIN_PATH="/usr/local/share/kafka_connect/plugins"
ENV CONNECT_KEY_CONVERTER="io.confluent.connect.avro.AvroConverter"
ENV CONNECT_VALUE_CONVERTER="io.confluent.connect.avro.AvroConverter"
ENV CONNECT_INTERNAL_KEY_CONVERTER="org.apache.kafka.connect.json.JsonConverter"
ENV CONNECT_INTERNAL_VALUE_CONVERTER="org.apache.kafka.connect.json.JsonConverter"

EXPOSE 9400
EXPOSE 80

CMD /opt/kafka/bin/connect-mirror-maker.sh  /home/kafka-to-es.properties

