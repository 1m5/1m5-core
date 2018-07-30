# Bus
Framework for the decoupling of clients, services, and sensors.

## Service Bus
Encompasses all functionality needed to support messaging between all internal services and their life cycles.
Provides a Staged Event-Driven Architecture (SEDA) by providing channels to/from all Services.
All bus threads (Worker Thread) come from one pool (Worker Thread Pool) to help manage resource usage.

### Configuration (bus.config)
- **1m5.bus.maxMessagesCachedMultiplier**: multiplies this value with the max threads to come up with the max number of cached messages 

### Start
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

### Send
When requests are made to send Envelope messages to the Bus, if the Worker Thread Pool is running,
it sends the Envelope to the Message Channel otherwise it adds an error message to the envelope and logs a warning.

### Shutdown
When the Service Bus shuts down, it currently performs the following tasks:

1. sets status to STOPPING
2. shuts down Worker Thread Pool
3. shuts down Message Channel
4. shuts down running services in parallel in separate threads
5. sets status to STOPPED

## Message Channel
Backed by a Blocking Queue, it acts as a Message Producer sending Envelope messages to the blocking queue while
also supporting Life Cycle methods to manage the queue. Graceful shutdown needs implemented to allow messages to
complete their routes prior to shutting down. Message persistence will be added with development of the Persistent
Message Queue.

### Start
Creates an ArrayBlockingQueue with its capacity set to the Bus's max number of cached messages 
and sets its accepting status to true;

### Send
When a send request is made, if it's accepting, it adds the Envelope to queue, 
otherwise it adds an error message to the Envelope and logs a warning.
If it's not accepting messages yet, it adds an error message to the Envelope and logs a warning.

### Receive
A receive request is a blocking request on the underlying queue. When a message arrives in the queue, it will
allow the calling thread to take it allowing it to continue on. An additional receive method is provided that
takes a timeout in milliseconds so that if a message isn't placed in the queue within the time alloted, it will
throw an InterruptedException to allow the thread to continue on without a message.

### Shutdown
Currently just sets accepting status to false so that no further messages will be accepted yet allowing any
queued messages to continue on.

## Worker Thread
This is instantiated by the Worker Thread Pool. The pool then calls its own execute method with that instance 
which internally calls the Worker Thread's start method resulting in the JVM alloting a new Thread then 
calling its run method.

### Run
The Worker Thread calls the Message Channel's receive method which blocks waiting for an Envelope message to show up in its
queue. When a message arrives, a reference to the Envelope is received and the following tasks are accomplished: 

1. If the Envelope indicates it's ready for reply to client, the Client App Manager is notified with the Envelope.
2. If the Envelope is not ready to return to a client and its Route indicates it's been routed, it is sent to the Orchestration Service.
3. Otherwise it is sent to the Service indicated in its Route.
4. The Envelope message is sent to the selected service by its Message Consumer interface's receive method.
5. If the message is received successfully, the Worker Thread acknowledges with the Message Channel that the Service now has it so that the Message Channel can remove it from the queue.
6. If the message is not received by the selected services successfully, it is re-attempted up to 3 times waiting 1 second between attempts.
7. 3 failed attempts results in Logging a warning. Future work needs to move the entire message to a failed log so that it can be retried again later.

## Worker Thread Pool
When the Worker Thread Pool is instantiated by the Service Bus, it takes as parameters:

- Client App Manager
- Map of running services
- Message Channel
- pool size
- max pool size
- Properties

The Worker Thread Pool is started in a new Thread by the JVM calling its run method. This is initiated by the Service Bus
creating an instance of the Worker Thread Pool and then calling its start method.

### Run
1. status set to Starting
2. a new fixed thread pool limited to max pool size created
3. status set to Running
4. while running, synchronously check if there are messages in the queue and if so, launch a Worker Thread to handle it
5. when Worker Thread Pool's status is no longer Running, release thread

### Shutdown
1. status set to Stopping
2. shutdown fixed thread pool
3. if fixed thread pool doesn't shutdown within 60 seconds
    1. initiate shutdownNow on pool
    2. if fixed thread pool doesn't shutdown within 60 seconds just continue
4. set status to Stopped
