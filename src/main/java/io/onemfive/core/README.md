# 1M5 Core
Decouples clients, services, and sensors to provide a staged event-driven
architecture (SEDA) built on micro-services (SOA) to support 3rd party
service providers to enable easy integration with strong privacy sensors.

## Common Classes
Classes used throughout all components are:

- **BaseService**: An abstract class that implements the basic functionality for Message Consumer, Service, and Life Cycle interfaces. All registered services must extend this class otherwise a ServiceNotSupportedException will be thrown on attempt to register.
- **Config**: Concrete class with static methods supports loading and saving property files as configurations from/to the classpath.
- **LifeCycle**: Interface with lifecycle methods for components including: start, pause, unpause, restart, shutdown, gracefulShutdown. Components should at least implement start, shutdown, and gracefulShutdown.
- **MessageConsumer**: Interface for components to receive Envelope messages. All services by extending BaseService are a MessageConsumer.
- **MessageProducer**: Interface for components to send Envelop messages. The ServiceBus is a MessageProducer referenced by all Services through BaseService, by the ClientAppManager, and by the SimpleClient.
- **OneMFiveAppContext**: A scope for using 1M5. Each application shares a 1M5 context through obtaining the global context instance using getInstance().
- **Service**: Interface for all BaseService extensions to implement handling Envelopes contain Documents, Events, Commands, and Header-only messages. BaseService implements all messages with a warning log that it's not implemented yet.
- **ServiceRegistrar**: Interface for registering and unregistering services. Currently implemented by ServiceBus.
- **ServiceRequest**: Base abstract class providing all service requests an error message and exception if needed.
- **ServiceStatusListener**: Interface for receiving service status'.

## Components
Primary components are the bus, clients, services, and sensors.

### [Admin Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/admin/README.md)
It supports registration of services with the service bus. 
Long-term it will support additional administration operations for the entire framework.

### [Aten Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/aten/README.md)
Not implemented although it's expected to be implemented shortly. 
Provides utility tokens for developers, both business and technical, 
to determine Prana distribution ratios from transaction fees if/when present.

### [Bus](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/bus/README.md)
The framework implemented by the Service Bus, Message Channel, Worker Thread, and Worker Thread Pool.
A Persistent Message Queue is expected in the future to ensure messages are persistent to aid in fault tolerance.

### [Client](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/client/README.md)
A package containing classes for clients to make requests from the Bus and receive replies when embedding 1M5.

- **Client**: An interface for clients to make requests and receive replies. Implemented by SimpleClient and received from Client App Manager's getClient method.
- **ClientAppManager**: Best method to receive an instance of this class is to use OneMFiveAppContext's getClientAppManager method. This class will start the Service Bus if it's stopped. It also ensures a SimpleClient is available. When stopping it, it will also stop the Service Bus. When you receive a Client from this object, ensure that you unregister it with this object too.
- **SimpleClient**: Returned from ClientAppManager as Client. Sends messages to Service Bus which releases the calling thread when the message reaches the Message Channel's queue. If a ServiceCallback is provided, SimpleClient uses an internal claim check using the Envelope's ID as the claim ID so that on notify the Envelope provided can be correlated with the requesting ServiceCallback which then calls its reply method.

### [Consensus Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/consensus/README.md)
Expected to provide consensus among mobiles.

Not Yet Implemented.

### [Content Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/content/README.md)
Will basically provide dynamic content through NodeJS as a back-end service for providing HTML5/CSS3/JS apps in Java and static content through an internal DHT.

Not Yet Implemented.

### [Contract Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/contract/README.md)
Expected to initially support Omni (Bitcoin) smart contracts but long-term desired to run smart contracts directly in this app.

Not Yet Implemented. 

### [Decentralized EXchange (DEX) Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/dex/README.md)
Integration of BarterDEX in the future.

Not Yet Implemented. 

### [Decentralized IDentity (DID) Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/did/README.md)
Self-Sovereign Identity + RepBAC (Reputation Based Access Control) + Circles of Influence. Identity without claimed authority, 
the best protection against abuse, overreach, and authoritarianism. The foundation for security, the DID is 
decentralized and guarantees privacy for every life and automaton on the planet returning control and ownership 
of personal information back to each of us while supporting access control based on reputation. 

### [InfoVault Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/infovault/README.md)
Vault of personal information to assist in building up your reputation and for additional services as you see fit. 
Access revocable on your command.

Currently uses Nitrite NoSQL embedded database used for persisting and loading into memory 1M5 data entities.
Needs to be seriously redesigned towards a JanusGraph over Cassandra on 10 billion mobiles with response times < 500ms.

### [IPFS Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/ipfs/README.md)
Acts as an API for IPFS. 
Supports local IPFS nodes, making calls to remote gateways, and acting as a gateway.
IPFS doesn't support anonymous communications inter-node and encryption must be provided.
Privacy isn't very strong and thus IPFS should only be used for public information without fear of censorship or retaliation.

### [KeyRing Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/ipfs/README.md)
Key management for securing your keys on your mobile and off-line. 
Supports sending keys to and receiving keys from off-line key vaults. 

Not Yet Implemented.

### [Orchestration Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/orchestration/README.md)
Orchestrates service calls using Simple Routes and Dynamic Routing Slips.
All Envelopes have a Dynamic Routing Slip but their current Route is nullable.
Dynamic Routing Slip is backed by a Deque Stack of Routes.
Routes can be added at any time, pushed to the stack (only supports Simple Routes for now).
Routes are popped off the stack when the Orchestrations retrieves the next Route.

Routes Envelopes accordingly:

1. If slip is not in progress, remaining routes counter is incremented by the number of remaining routes in the slip, and the slip is flagged as in progress.
2. If there's a Route ready to be popped off the stack, the Route is popped, set in the Envelope, and active routes counter is incremented.
3. If there's no additional Routes, there was no Route set in the Envelope, or there was but it's already been routed, or there was and it hasn't routed but it's for the Orchestration Service, then if client id was set in Envelope, set Reply to client as true otherwise just end the route, but either way decrement the active routes counter and remaining routes counter. If the route is just a fire-and-forget, just send it on its way and increment both the active and remaining routes counters.

### [Prana Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/prana/README.md)
Utility tokens used in the platform as a method to manage resources (network, cpu, storage) to support it. 
Prana can be acquired by sharing resources, selling personal information, and as rewards for using the system. 
They can be used to acquire resources beyond what a user's mobile provides.

Not Yet Implemented.

### [Repository Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/repository/README.md)
Provides access to various decentralized repository services.

The first implementation is for GitLab using GitLab.com as a temporary hosting solution. Long-term it's expected to support Git fully decentralized.

Started but not yet implemented.

### [SecureDrop Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/securedrop/README.md)
Simplifies submitting sensitive data to Secure Drop service via clearnet or Tor.

Not Yet Implemented.

### [Sensors Service](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/sensors/README.md)
Provides an intelligent router as an overlay network using I2P for a base level of anonymity P2P and Tor when accessing clearnet web services. 
The module participates with the DID Service for self-sovereign identity and reputation based access. 
The system survives even if the internet goes down or is cut off by supporting peer-to-peer direct wireless mesh networks. 
As long as enough people still have their device, the network survives. 

### [Utilities](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/util/README.md)

Many of the current utilities are a collection of code that need to be placed in with the appropriate component unless
truly general in utility.