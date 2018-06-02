# core
Updated as of 0.5.0.
An intelligent anonymous communications framework with popular general decentralized services.
Components consist of the bus framework, internal services, sensors, and utilities.
Classes at this level include:

- BaseService: An abstract class that implements the basic functionality for Message Consumer, Service, and Life Cycle interfaces. All registered services must extend this class otherwise a ServiceNotSupportedException will be thrown on attempt to register.
- Config: Concrete class with static methods supports loading and saving property files as configurations from/to the classpath.
- LifeCycle: Interface with lifecycle methods for components including: start, pause, unpause, restart, shutdown, gracefulShutdown. Components should at least implement start, shutdown, and gracefulShutdown.
- MessageConsumer: Interface for components to receive Envelope messages. All services by extending BaseService are a MessageConsumer.
- MessageProducer: Interface for components to send Envelop messages. The ServiceBus is a MessageProducer referenced by all Services through BaseService, by the ClientAppManager, and by the SimpleClient.
- OneMFiveAppContext: A scope for using 1M5. Each application shares a 1M5 context through obtaining the global context instance using getInstance().
- Service: Interface for all BaseService extensions to implement handling Envelopes contain Documents, Events, Commands, and Header-only messages. BaseService implements all messages with a warning log that it's not implemented yet.
- ServiceRegistrar: Interface for registering and unregistering services. Currently implemented by ServiceBus.

## Components
1M5 is composed of a Service-Oriented Architecture (SOA) design using a service bus for micro-services, 
a Staged Event-Driven Architecture (SEDA) design for asynchronous multi-threaded inter-service communications,
a service registry, and number of Sensors for advanced intelligent interaction with other nodes.

### Admin Service
It supports registration of services with the service bus. 
Long-term it will support additional administration operations for the entire framework.

### Aten Service
Not implemented althogh it's expected to be implemented shortly. 
Provides utility tokens for developers, both business and technical, 
to determine Prana distribution ratios from transaction fees if/when present.

### Bus
The framework implemented by the Service Bus, Message Channel, Worker Thread, and Worker Thread Pool.
A Persistent Message Queue is expected in the future to ensure messages are persistent to aid in fault tolerance.

#### Service Bus

#### Message Channel

#### Worker Thread

#### Worker Thread Pool

### Client

### Consensus Service

### Content Service

### Contract Service

### Decentralized EXchange (DEX) Service

### Decentralized IDentity (DID) Service

### InfoVault Service

### IPFS Service

### KeyRing Service

### Orchestration Service

### Payment Service

### Prana Service

### Repository Service

### SecureDrop Service

### Sensors Service

#### Aggression Filter

#### Bluetooth Sensor

#### Bluetooth Low-Energy Sensor

#### Cleaner Service

##### Amazon Cleaner

##### Facebook Cleaner

##### Google Cleaner

##### Keylogger Cleaner

##### Surveillance Cleaner

##### Twitter Cleaner

#### Clearnet Sensor

#### CSploit Counter-Measures

#### HAM Sensor

#### Honey Pot Counter-Measures

#### I2P Sensor

##### Attack Mitigation

- https://www.irongeek.com/i.php?page=security/i2p-identify-service-hosts-eepsites

#### I2P Bote Sensor

#### IMSI Filter Catcher

##### Research

- https://techcrunch.com/2017/06/02/who-catches-the-imsi-catchers-researchers-demonstrate-stingray-detection-kit/

#### Mesh Sensor

##### RightMesh

#### Nearby Sensor

#### Near-Field Communications (NFC) Sensor

#### Redtooth Sensor

#### Rooting

#### Tor Sensor

#### WiFi Aware Sensor

#### WiFi Direct Sensor

#### WiFi HaLow Sensor

#### WiFi Internet Sensor

#### WiFi Utilities

### Utilities

Many of the current utilities are a collection of code that need to be placed in with the appropriate component unless
truly general in utility.

