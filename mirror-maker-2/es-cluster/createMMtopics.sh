/opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka1:9092 --list
/opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka1:9092 --create  --replication-factor 1 --partitions 1 --topic source.products
