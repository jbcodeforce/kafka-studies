# Event Mesh

Enterprises need to reliably deliver a high volume of events across global networks, traversing hybrid clouds 
and on-premise applications while avoiding bottlenecks. 

The event mesh is dynamic infrastructure that propagates events across disparate cloud platforms and performs protocol
translation. 

Capabilities include: 

* Support for ingress and egress of events in various transports, such as Kafka, Knative, HTTP,
AMQP, and others
* Fault tolerance for high-reliability delivery of messages, including automated recovery from network failures and fallback destinations for undeliverable messages
* Support for multi-protocol bridges between disparate events, applications, and messaging platforms
* Support for on-premises and multi-cloud deployment to provide a uniform infrastructure across the enterprise
* Multiple client APIs for a wide range of programming languages and environments
* Support for Multicast (all subscribers receive a copy of each message) or anycast (one subscriber receives a copy of each produced message)
* Management console for the administration of the mesh and monitoring of activity
* Secure transmission of event messages

Events can flow bidirectionally across the multi-cloud topology. As more applications produce and consume events, 
a key feature of the event mesh is that events published by any application in any programming environment (e.g. a Java JMS e
event) in one cloud can be consumed by an application on another cloud using a different API.

Event mesh supports application developers by alleviating concerns about the location of consumers across local, regional, and global distributed topologies to
support loosely coupled event-driven use cases.

There is no product on the market addressing those capabilities still the products that can support it:

* Kafka, MQ, Mirror Maker 2
* Cloud Events
* Reactive messaging
* Knative
* Egeria for knowing what is where
