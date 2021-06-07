import faust
'''
Faust is a kafka streaming api
'''
import EventBackboneConfiguration as EventBackboneConfiguration

class Greeting(faust.Record):
    from_name: str
    to_name: str

'''
'kafka://broker-0-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093',
        'kafka://broker-1-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093',
        'kafka://broker-2-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093',
        'kafka://broker-4-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093',
        'kafka://broker-5-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093'

        ,
        broker_credentials=faust.SASLCredentials(
            username='token',
            password=EventBackboneConfiguration.getEndPointAPIKey(),
        )
'''
print(EventBackboneConfiguration.getEndPointAPIKey())
app = faust.App('hello-app', 
        broker=['kafka://' + EventBackboneConfiguration.getBrokerEndPoints()
        ]
        )
topic = app.topic('TestTopic', value_type=Greeting)

@app.agent(topic)
async def hello(greetings):
    async for greeting in greetings:
        print(f'Hello from {greeting.from_name} to {greeting.to_name}')

@app.timer(interval=1.0)
async def example_sender(app):
    await hello.send(
        value=Greeting(from_name='Faust', to_name='you'),
    )

if __name__ == '__main__':
    app.main()