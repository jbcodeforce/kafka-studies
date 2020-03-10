# Send basic test message to TestTopic
from confluent_kafka import Producer, KafkaError
import json
import EventBackboneConfiguration as EventBackboneConfiguration

options ={
    'bootstrap.servers': EventBackboneConfiguration.getBrokerEndPoints(),
    'group.id': 'BasicProducer'
}
if (EventBackboneConfiguration.hasAPIKey()):
    options['security.protocol'] = 'SASL_SSL'
    options['sasl.mechanisms'] = 'PLAIN'
    options['sasl.username'] = 'token'
    options['sasl.password'] = EventBackboneConfiguration.getEndPointAPIKey()

if (EventBackboneConfiguration.isEncrypted()):
    options['ssl.ca.location'] = EventBackboneConfiguration.getKafkaCertificate()

print('[KafkaProducer] - {}'.format(options))
producer=Producer(options)

def delivery_report(err, msg):
        """ Called once for each message produced to indicate delivery result.
            Triggered by poll() or flush(). """
        if err is not None:
            print('[ERROR] - [KafkaProducer] - Message delivery failed: {}'.format(err))
        else:
            print('[KafkaProducer] - Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

def publishEvent(topicName, valueToSend, keyToSend):
    dataStr = json.dumps(valueToSend)
    producer.produce(topicName,
        key=keyToSend,
        value=dataStr.encode('utf-8'), 
        callback=delivery_report)
    producer.flush()

if __name__ == "__main__":
    publishEvent(EventBackboneConfiguration.getTopicName(),
        "Hello bill", "B01")