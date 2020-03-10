# Trace orders in kafka orders topic
from confluent_kafka import Consumer, KafkaError
import EventBackboneConfiguration as EventBackboneConfiguration

options ={
    'bootstrap.servers': EventBackboneConfiguration.getBrokerEndPoints(),
    'group.id': 'BasicConsumer',
    'auto.offset.reset': 'earliest',
    'enable.auto.commit': 'True'
}
if (EventBackboneConfiguration.hasAPIKey()):
    options['security.protocol'] = 'SASL_SSL'
    options['sasl.mechanisms'] = 'PLAIN'
    options['sasl.username'] = 'token'
    options['sasl.password'] = EventBackboneConfiguration.getEndPointAPIKey()

if (EventBackboneConfiguration.isEncrypted()):
    options['ssl.ca.location'] = EventBackboneConfiguration.getKafkaCertificate()

print('[KafkaConsumer] - {}'.format(options))
c = Consumer(options)
c.subscribe([EventBackboneConfiguration.getTopicName()])
print("Ready to get message from " + EventBackboneConfiguration.getTopicName())
while True:
    msg = c.poll(1.0)

    if msg is None:
        continue
    if msg.error():
        print("Consumer error: {}".format(msg.error()))
        continue

    print('Received message: {}'.format(msg.value().decode('utf-8')))

c.close()