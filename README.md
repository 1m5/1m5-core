# 1M5 Core
An intelligent anonymous communications framework with popular general decentralized services and easy to use APIs
for embedding.

## Version
0.5.1

## Authors / Developers
- [ObjectOrange](https://github.com/objectorange) - objectorange@protonmail.com

## Opportunities
[**Freedom of Speech**](https://en.wikipedia.org/wiki/Freedom_of_speech) - a principle that supports the freedom of 
an individual or a community to articulate their  opinions and ideas without fear of retaliation, censorship, 
or sanction. The term "freedom of expression" is sometimes used synonymously but includes any act of seeking, 
receiving, and imparting information or ideas, regardless of the medium used.

[**Censorship**](https://en.wikipedia.org/wiki/Censorship) - the suppression of speech, public communication, 
or other information, on the basis that such material is considered objectionable, harmful, sensitive, 
politically incorrect or "inconvenient" as determined by government authorities or by community consensus.

Constraining the free flow of information between people is a direct threat to our freedom and censorship of 
communications on-line is growing world-wide.

- https://internetfreedomwatch.org/timeline/
- https://www.wired.com/2017/04/internet-censorship-is-advancing-under-trump/
- https://rsf.org/en/news/more-100-websites-blocked-growing-wave-online-censorship 

On-line communications are censored at the point of entrance by Internet Service Providers (ISP). 
They act as gateways to the internet providing governments control over speech by having the
ability to restrict usage and track people's usage via their leased IP addresses. In order to make tracking usage much more
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

In addition:

- Most organizations today (e.g. Tech, Banks, Governments, Hospitals) track, persist, and use our behavior for their profit not ours.
- Centralized organizations are major targets for theft.
- Closed source software can easily contain hidden back doors for thieves to access our information without our knowledge and many open source applications have closed source libraries embedded in them.
- Whistleblowers, the abused, visible minorities, and a myriad of other people could be emboldened by anonymity to speak out in a manner that would otherwise be unavailable if they were forced to identify themselves.
- Blockchain based applications and cryptocurrencies like Bitcoin are helping to wrestle some control from centralized organizations although they are largely used on servers and distributed ledgers are still logically centralized and difficult to maintain anonymity at the network layer.
- Smartphone ownership around the world is greater than PC ownership.
- Smartphones, our primary means of global communication and collaboration, are weak in maintaining our anonymity and privacy - critical to ensuring individual freedom.

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
Android is a subset of Java's JDK.

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
Not implemented although it's expected to be implemented shortly. 
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
When the Worker Thread Pool is instantiated by the Service Bus, it takes as parameters:

- Client App Manager
- Map of running services
- Message Channel
- pool size
- max pool size
- Properties

The Worker Thread Pool is started in a new Thread by the JVM calling its run method. This is initiated by the Service Bus
creating an instance of the Worker Thread Pool and then calling its start method.

###### Run
1. status set to Starting
2. a new fixed thread pool limited to max pool size created
3. status set to Running
4. while running, synchronously check if there are messages in the queue and if so, launch a Worker Thread to handle it
5. when Worker Thread Pool's status is no longer Running, release thread

###### Shutdown
1. status set to Stopping
2. shutdown fixed thread pool
3. if fixed thread pool doesn't shutdown within 60 seconds
    1. initiate shutdownNow on pool
    2. if fixed thread pool doesn't shutdown within 60 seconds just continue
4. set status to Stopped

#### Client
A package containing classes for clients to make requests from the Bus and receive replies when embedding 1M5.

- **Client**: An interface for clients to make requests and receive replies. Implemented by SimpleClient and received from Client App Manager's getClient method.
- **ClientAppManager**: Best method to receive an instance of this class is to use OneMFiveAppContext's getClientAppManager method. This class will start the Service Bus if it's stopped. It also ensures a SimpleClient is available. When stopping it, it will also stop the Service Bus. When you receive a Client from this object, ensure that you unregister it with this object too.
- **SimpleClient**: Returned from ClientAppManager as Client. Sends messages to Service Bus which releases the calling thread when the message reaches the Message Channel's queue. If a ServiceCallback is provided, SimpleClient uses an internal claim check using the Envelope's ID as the claim ID so that on notify the Envelope provided can be correlated with the requesting ServiceCallback which then calls its reply method.

#### Consensus Service
Expected to provide consensus among mobiles.

Not Yet Implemented.

#### Content Service
Will basically provide dynamic content through NodeJS as a back-end service for providing HTML5/CSS3/JS apps in Java and static content through integration with IPFS.

Not Yet Implemented.

#### Contract Service
Expected to initially support Ethereum and Omni (Bitcoin) smart contracts but long-term desired to run smart contracts directly in this app.

Not Yet Implemented. 

#### Decentralized EXchange (DEX) Service
Researching options.

Not Yet Implemented. 

#### Decentralized IDentity (DID) Service
Self-Sovereign Identity + RepBAC (Reputation Based Access Control) + Circles of Influence. Identity without claimed authority, 
the best protection against abuse, overreach, and authoritarianism. The foundation for security, the DID is 
decentralized and guarantees privacy for every life and automaton on the planet returning control and ownership 
of personal information back to each of us while supporting access control based on reputation. 

Only implementing username/passphrase functionality currently.

Needs to move towards embedding OpenPGP and then integrating additional decentralized identity services.

Currently working I2P Bote identities in by default for P2P communications.

- **Verify**: The Verify Operation checks to see if a DID is available in the InfoVault by supplied alias.
If so, it loads the whole DID into the supplied Envelope replacing the supplied DID and sets the DID's verified flag to true
else it flags the supplied DID's verified flag to false.
- **Authenticate**: The Authenticate Operations performs the same as Verify but includes passphrase checking setting authenticated flag instead.
- **Create**: The Create Operation creates a new DID with supplied alias and passphrase saving to the InfoVault
returning the resultant DID in the Envelope.

#### InfoVault Service
Vault of personal information to assist in building up your reputation and for additional services as you see fit. 
Access revocable on your command.

Currently uses Nitrite NoSQL embedded database used for persisting and loading into memory 1M5 data entities.
Needs to be seriously redesigned towards a JanusGraph over Cassandra on 10 billion mobiles with response times < 500ms.

#### IPFS Service
Acts as an API for IPFS. 
Supports local IPFS nodes, making calls to remote gateways, and acting as a gateway.

#### KeyRing Service
Key management for securing your keys on your mobile and off-line. 
Supports sending keys to and receiving keys from off-line key vaults. 

Not Yet Implemented.

#### Orchestration Service
Orchestrates service calls using Simple Routes and Dynamic Routing Slips.
All Envelopes have a Dynamic Routing Slip but their current Route is nullable.
Dynamic Routing Slip is backed by a Deque Stack of Routes.
Routes can be added at any time, pushed to the stack (only supports Simple Routes for now).
Routes are popped off the stack when the Orchestrations retrieves the next Route.

Routes Envelopes accordingly:

1. If slip is not in progress, remaining routes counter is incremented by the number of remaining routes in the slip, and the slip is flagged as in progress.
2. If there's a Route ready to be popped off the stack, the Route is popped, set in the Envelope, and active routes counter is incremented.
3. If there's no additional Routes, there was no Route set in the Envelope, or there was but it's already been routed, or there was and it hasn't routed but it's for the Orchestration Service, then if client id was set in Envelope, set Reply to client as true otherwise just end the route, but either way decrement the active routes counter and remaining routes counter. If the route is just a fire-and-forget, just send it on its way and increment both the active and remaining routes counters.

#### Payment Service
Expected to integrate with varies crypto-currencies for payments.

Not Yet Implemented.

#### Prana Service
Utility tokens used in the platform as a method to manage resources (network, cpu, storage) to support it. 
Prana can be acquired by sharing resources, selling personal information, and as rewards for using the system. 
They can be used to acquire resources beyond what a user's mobile provides.

Not Yet Implemented.

#### Repository Service
Provides access to various decentralized repository services.

The first implementation is for GitLab using GitLab.com as a temporary hosting solution. Long-term it's expected to support Git fully decentralized.

Started but not yet implemented.

#### SecureDrop Service
Simplifies submitting sensitive data to Secure Drop service via clearnet or Tor.

Not Yet Implemented.

#### Sensors Service
Provides an intelligent router as an overlay network using I2P for a base level of anonymity P2P and Tor when accessing clearnet web services. 
The module participates with the DID Service for self-sovereign identity and reputation based access. 
The system survives even if the internet goes down or is cut off by supporting peer-to-peer direct wireless mesh networks. 
As long as enough people still have their device, the network survives. 

##### Configuration

##### Start

##### Shutdown

##### Graceful Shutdown

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
An embedded I2P Router.
Not ready for usage.

###### Attack Mitigation

- https://www.irongeek.com/i.php?page=security/i2p-identify-service-hosts-eepsites

##### I2P Bote Sensor
Uses embedded I2P Router adding storable DHT for delayed routing to battle timing attacks.
Started when 1m5.sensors.registered property in sensors.config contains bote.
Connects to I2P and has been verified to send messages to/from the same address.

###### Build Notes
- Required flex-gmss-1.7p1.jar in libs folder to be added to local Maven .m2 directory:
mvn install:install-file -Dfile=flexi-gmss-1.7p1.jar -DgroupId=de.flexi -DartifactId=gmss -Dversion=1.7p1 -Dpackaging=jar
- Required certificates from the following two directories in the i2p.i2p project (I2P Router core)
to be copied to resources/io/onemfive/core/sensors/i2p/bote/certificates keeping reseed and ssl as directories:
    - /installer/resources/certificates/reseed
    - /installer/resources/certificates/ssl

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
An embedded Tor Router.
Not yet embedded.

##### WiFi Aware Sensor

##### WiFi Direct Sensor

##### WiFi HaLow Sensor

##### WiFi Internet Sensor

##### WiFi Utilities

#### Utilities

Many of the current utilities are a collection of code that need to be placed in with the appropriate component unless
truly general in utility.

