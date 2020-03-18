 TGT_PROP_FILE=/home/local-cluster/mirrormaker2.properties
 cat  /home/local-cluster/localkafka-to-es-mm2.properties | sed  -e "s/KAFKA_SOURCE_BROKERS/$KAFKA_SOURCE_BROKERS/g" \
 -e "s/KAFKA_TARGET_BROKERS/$KAFKA_TARGET_BROKERS/g" \
 -e  "s/KAFKA_TARGET_APIKEY/$KAFKA_TARGET_APIKEY/g" > $TGT_PROP_FILE
 cat $TGT_PROP_FILE
 export LOG_DIR=/tmp/logs
 /opt/kafka/bin/connect-mirror-maker.sh $TGT_PROP_FILE
