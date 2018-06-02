# 1M5 Core
Updated as of 0.5.0.
An intelligent anonymous communications framework with popular general decentralized services.
Components consist of the bus framework, internal services, sensors, and utilities.

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

### Common Classes
Classes at this level include:

- **BaseService**: An abstract class that implements the basic functionality for Message Consumer, Service, and Life Cycle interfaces. All registered services must extend this class otherwise a ServiceNotSupportedException will be thrown on attempt to register.
- **Config**: Concrete class with static methods supports loading and saving property files as configurations from/to the classpath.
- **LifeCycle**: Interface with lifecycle methods for components including: start, pause, unpause, restart, shutdown, gracefulShutdown. Components should at least implement start, shutdown, and gracefulShutdown.
- **MessageConsumer**: Interface for components to receive Envelope messages. All services by extending BaseService are a MessageConsumer.
- **MessageProducer**: Interface for components to send Envelop messages. The ServiceBus is a MessageProducer referenced by all Services through BaseService, by the ClientAppManager, and by the SimpleClient.
- **OneMFiveAppContext**: A scope for using 1M5. Each application shares a 1M5 context through obtaining the global context instance using getInstance().
- **Service**: Interface for all BaseService extensions to implement handling Envelopes contain Documents, Events, Commands, and Header-only messages. BaseService implements all messages with a warning log that it's not implemented yet.
- **ServiceRegistrar**: Interface for registering and unregistering services. Currently implemented by ServiceBus.

### Components
1M5 is composed of a Service-Oriented Architecture (SOA) design using a service bus for micro-services, 
a Staged Event-Driven Architecture (SEDA) design for asynchronous multi-threaded inter-service communications,
a service registry, and a number of Sensors for advanced intelligent interaction with other nodes.

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
All bus threads (Worker Thread) come from one pool to help manage resource usage.

##### Message Channel

##### Worker Thread

##### Worker Thread Pool

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

