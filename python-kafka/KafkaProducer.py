import json, os
from confluent_kafka import KafkaError, Producer
import kafka.EventBackboneConfiguration as ebc

class KafkaProducer:

    def __init__(self,
                kafka_brokers = "", 
                kafka_apikey = "", 
                kafka_cacert = "", 
                topic_name = ""):
        self.kafka_brokers = kafka_brokers
        self.kafka_apikey = kafka_apikey
        self.kafka_cacert = kafka_cacert
        self.topic_name = topic_name

    def prepare(self,groupID = "pythonproducers"):
        options ={
                'bootstrap.servers':  self.kafka_brokers,
                'group.id': groupID
        }
        if (self.kafka_apikey != ''):
            options['security.protocol'] = 'SASL_SSL'
            options['sasl.mechanisms'] = 'PLAIN'
            options['sasl.username'] = 'token'
            options['sasl.password'] = self.kafka_apikey
        if (self.kafka_cacert != ''):
            options['ssl.ca.location'] = self.kafka_cacert
        print("[KafkaProducer] - This is the configuration for the producer:")
        print('[KafkaProducer] - {}'.format(options))
        self.producer = Producer(options)

    def delivery_report(self,err, msg):
        """ Called once for each message produced to indicate delivery result.
            Triggered by poll() or flush(). """
        if err is not None:
            print('[ERROR] - [KafkaProducer] - Message delivery failed: {}'.format(err))
        else:
            print('[KafkaProducer] - Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

    def publishEvent(self, eventToSend, keyName):
        dataStr = json.dumps(eventToSend)
        self.producer.produce(self.topic_name,
                            key=eventToSend[keyName],
                            value=dataStr.encode('utf-8'), 
                            callback=self.delivery_report)
        self.producer.flush()

def processRecords(TOPICNAME,GROUPID,KEYNAME,docsToSend):
    print("Producer to the topic " + TOPICNAME)
    try:
        producer = KafkaProducer(kafka_brokers = ebc.getBrokerEndPoints(), 
                kafka_apikey = ebc.getEndPointAPIKey(), 
                kafka_cacert = ebc.getKafkaCertificate(),
                topic_name = TOPICNAME)

        producer.prepare(groupID= GROUPID)
        for doc in docsToSend:
            print("sending -> " + str(doc))
            producer.publishEvent(doc,KEYNAME)
    except KeyboardInterrupt:
        input('Press enter to continue')
        print("Thank you")