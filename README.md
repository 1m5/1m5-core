# 1M5 Core
An intelligent anonymous communications framework with popular general decentralized services and easy to use APIs
for embedding.

## Version
0.5.0

## Authors
- ObjectOrange - objectorange@protonmail.com

## Opportunities
Censorship of communications on-line is growing world-wide.

- https://internetfreedomwatch.org/timeline/
- https://www.wired.com/2017/04/internet-censorship-is-advancing-under-trump/
- https://rsf.org/en/news/more-100-websites-blocked-growing-wave-online-censorship

Internet Service Providers (ISP) act as gateways to the internet providing governments control over speech by having the
ability to restrict usage and track people's usage via their loaned IP addresses. In order to make tracking usage much more
difficult, tools have come out that provide techniques called onion-/garlic-routing where the source and destinations of
internet routes can not be determined without breaking encryption, a very expensive feat, sometimes impossible today when
considering the encryption algorithms used. 

Two primary tools today that support this are Tor and I2P. Tor provides a browser
that makes it easier to use while I2P is much less known. Both are complementary in that Tor was designed for browsing
today's current web sites anonymously. I2P was designed for peer-to-peer communications within I2P. Neither have good
APIs for developers to embed in their products making uptake slow for many applications.

A third tool on the horizon is one that completely circumvents ISPs by not using them. They're called direct wireless
mesh networks and they can communicate directly phone-to-phone using technologies such as WiFi Direct. Firechat is an
example used during the 2014 Hong Kong protests after the Chinese government threatened to shutdown the internet in that
area. New mesh solutions are popping up including RightMesh that seek to improve on earlier designs. But the technology
is still in its infancy and needs to be pulled into ever day applications more easily once they've matured.

Even getting these technologies in wide use doesn't solve the problem of online censorship. People in governments, corporations, and
other thieves are constantly finding ways to circumvent these technologies to censor and steal information.

## Solution
1M5 works to solve these issues by providing an intelligent router embedding Tor, I2P, Direct Wireless Mesh, and other
networks, using them intelligently as one dynamic network, and providing easy to use APIs for developers to embed in their
applications. In addition, it provides access to commonly desired decentralized services in an anonymous fashion including
self-sovereign identities, IPFS, Ethereum, GitHub, and others in the future making integration a snap.

## Design
1M5 is composed of a Service-Oriented Architecture (SOA) design using a service bus for micro-services, 
a Staged Event-Driven Architecture (SEDA) design for asynchronous multi-threaded inter-service communications,
a service registry, internal general services, and a number of Sensors for advanced intelligent interaction with other nodes.

## Implementation
The application is written in Java using Android 23 JDK 1.7 to ensure the core can run in Android 5.0+.

### Common Classes
Classes used throughout all components are:

- **BaseService**: An abstract class that implements the basic functionality for Message Consumer, Service, and Life Cycle interfaces. All registered services must extend this class otherwise a ServiceNotSupportedException will be thrown on attempt to register.
- **Config**: Concrete class with static methods supports loading and saving property files as configurations from/to the classpath.
- **LifeCycle**: Interface with lifecycle methods for components including: start, pause, unpause, restart, shutdown, gracefulShutdown. Components should at least implement start, shutdown, and gracefulShutdown.
- **MessageConsumer**: Interface for components to receive Envelope messages. All services by extending BaseService are a MessageConsumer.
- **MessageProducer**: Interface for components to send Envelop messages. The ServiceBus is a MessageProducer referenced by all Services through BaseService, by the ClientAppManager, and by the SimpleClient.
- **OneMFiveAppContext**: A scope for using 1M5. Each application shares a 1M5 context through obtaining the global context instance using getInstance().
- **Service**: Interface for all BaseService extensions to implement handling Envelopes contain Documents, Events, Commands, and Header-only messages. BaseService implements all messages with a warning log that it's not implemented yet.
- **ServiceRegistrar**: Interface for registering and unregistering services. Currently implemented by ServiceBus.

### Components
Primary components are the bus, services, sensors, and utilities.

#### Admin Service
It supports registration of services with the service bus. 
Long-term it will support additional administration operations for the entire framework.

#### Aten Service
Not implemented althogh it's expected to be implemented shortly. 
Provides utility tokens for developers, both business and technical, 
to determine Prana distribution ratios from transaction fees if/when present.

#### Bus
The framework implemented by the Service Bus, Message Channel, Worker Thread, and Worker Thread Pool.
A Persistent Message Queue is expected in the future to ensure messages are persistent to aid in fault tolerance.

##### Service Bus
Encompasses all functionality needed to support messaging between all internal services and their life cycles.
Provides a Staged Event-Driven Architecture (SEDA) by providing channels to/from all Services.
All bus threads (Worker Thread) come from one pool (Worker Thread Pool) to help manage resource usage.

###### Configuration (bus.config)
- **1m5.bus.maxMessagesCachedMultiplier**: multiplies this value with the max threads to come up with the max number of cached messages 

###### Start
When the Service Bus starts, it currently performs the following tasks:

1. sets status to STARTING
2. loads its configuration (bus.config)
3. sets parameters, e.g. maxMessagesCached, maxThreads
4. Message Channel is started
5. following services are started in parallel (ignoring maxThreads) in separate threads:
    - Admin Service
    - InfoVault Service
    - Orchestration Service
    - DID Service
    - IPFS Service
    - Sensors Service
6. starts Worker Thread Pool
7. sets status to RUNNING

###### Send
When requests are made to send Envelope messages to the Bus, if the Worker Thread Pool is running,
it sends the Envelope to the Message Channel otherwise it adds an error message to the envelope and logs a warning.

###### Shutdown
When the Service Bus shuts down, it currently performs the following tasks:

1. sets status to STOPPING
2. shuts down Worker Thread Pool
3. shuts down Message Channel
4. shuts down running services in parallel in separate threads
5. sets status to STOPPED

##### Message Channel
Backed by a Blocking Queue, it acts as a Message Producer sending Envelope messages to the blocking queue while
also supporting Life Cycle methods to manage the queue. Graceful shutdown needs implemented to allow messages to
complete their routes prior to shutting down. Message persistence will be added with development of the Persistent
Message Queue.

###### Start
Creates an ArrayBlockingQueue with its capacity set to the Bus's max number of cached messages 
and sets its accepting status to true;

###### Send
When a send request is made, if it's accepting, it adds the Envelope to queue, 
otherwise it adds an error message to the Envelope and logs a warning.
If it's not accepting messages yet, it adds an error message to the Envelope and logs a warning.

###### Receive
A receive request is a blocking request on the underlying queue. When a message arrives in the queue, it will
allow the calling thread to take it allowing it to continue on. An additional receive method is provided that
takes a timeout in milliseconds so that if a message isn't placed in the queue within the time alloted, it will
throw an InterruptedException to allow the thread to continue on without a message.

###### Shutdown
Currently just sets accepting status to false so that no further messages will be accepted yet allowing any
queued messages to continue on.

##### Worker Thread
This is instantiated by the Worker Thread Pool. The pool then calls its own execute method with that instance 
which internally calls the Worker Thread's start method resulting in the JVM alloting a new Thread then 
calling its run method.

###### Run
The Worker Thread calls the Message Channel's receive method which blocks waiting for an Envelope message to show up in its
queue. When a message arrives, a reference to the Envelope is received and the following tasks are accomplished: 

1. If the Envelope indicates it's ready for reply to client, the Client App Manager is notified with the Envelope.
2. If the Envelope is not ready to return to a client and its Route indicates it's been routed, it is sent to the Orchestration Service.
3. Otherwise it is sent to the Service indicated in its Route.
4. The Envelope message is sent to the selected service by its Message Consumer interface's receive method.
5. If the message is received successfully, the Worker Thread acknowledges with the Message Channel that the Service now has it so that the Message Channel can remove it from the queue.
6. If the message is not received by the selected services successfully, it is re-attempted up to 3 times waiting 1 second between attempts.
7. 3 failed attempts results in Logging a warning. Future work needs to move the entire message to a failed log so that it can be retried again later.

##### Worker Thread Pool
When the Worker Thread Pool is instantiated, it takes as parameters:

- Client App Manager
- Map of running services
- Message Channel
- pool size
- max pool size
- Properties

The Worker Thread Pool is started in a new Thread by the JVM calling its run method. This is initiated by the Service Bus
creating an instance of the Worker Thread Pool and then calling its start method.

###### Run


#### Client

#### Consensus Service

#### Content Service

#### Contract Service

#### Decentralized EXchange (DEX) Service

#### Decentralized IDentity (DID) Service

#### InfoVault Service

#### IPFS Service

#### KeyRing Service

#### Orchestration Service

#### Payment Service

#### Prana Service

#### Repository Service

#### SecureDrop Service

#### Sensors Service

##### Aggression Filter

##### Bluetooth Sensor

##### Bluetooth Low-Energy Sensor

##### Cleaner Service

###### Amazon Cleaner

###### Facebook Cleaner

###### Google Cleaner

###### Keylogger Cleaner

###### Surveillance Cleaner

###### Twitter Cleaner

##### Clearnet Sensor

##### CSploit Counter-Measures

##### HAM Sensor

##### Honey Pot Counter-Measures

##### I2P Sensor

###### Attack Mitigation

- https://www.irongeek.com/i.php?page=security/i2p-identify-service-hosts-eepsites

##### I2P Bote Sensor

##### IMSI Filter Catcher

###### Research

- https://techcrunch.com/2017/06/02/who-catches-the-imsi-catchers-researchers-demonstrate-stingray-detection-kit/

##### Mesh Sensor

###### RightMesh

##### Nearby Sensor

##### Near-Field Communications (NFC) Sensor

##### Redtooth Sensor

##### Rooting

##### Tor Sensor

##### WiFi Aware Sensor

##### WiFi Direct Sensor

##### WiFi HaLow Sensor

##### WiFi Internet Sensor

##### WiFi Utilities

#### Utilities

Many of the current utilities are a collection of code that need to be placed in with the appropriate component unless
truly general in utility.

