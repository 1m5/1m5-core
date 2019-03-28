# 1M5 Core
A secure open-source decentralized censorship-resistant peer-to-peer application platform with end-to-end encryption 
and anonymity as a base layer for creating easy to build and use secure decentralized peer-to-peer 
applications requiring no server connections that can be used around the world by any person looking 
to protect their communications and personal data from unethical monitoring, interception, intrusion, 
and censorship.

## Version
0.6.0

## Licensing
In the interests of ensuring the 1M5 mission, all copyright automatically imposed on the 1M5 project by any and all people
and organizations are removed and thus nullified. We believe this to be encompassed in the [Unlicense](https://unlicense.org/) statement.
All 1M5 services and sensors are created with the Unlicense statement by default unless otherwise specified,
e.g. 1m5-pureos is GPLv3 as that is the license preferred by Purism.

Bouncycastle is embedded in 1M5 Core and its MIT-like license is [here](http://www.bouncycastle.org/licence.html).

## Authors / Developers
* objectorange (Brian Taylor) - [GitHub](https://github.com/objectorange) | [LinkedIn](https://www.linkedin.com/in/decentralizationarchitect/) | PGP: DD08 8658 5380 C7DF 1B4E 04C2 1849 B798 CF36 E2AF
* evok3d (Amin Rafiee) - [Site](https://arafiee.com/) | PGP: D921 C2EE 60BA C264 EA40 4DC5 B6F8 2589 96AA E505
* azad (Erbil Kaplan) - [LinkedIn](https://www.linkedin.com/in/erbil-kaplan-b8971b18/)

## Bounties
Paid in Bitcoin

## Donations

### Bitcoin
1Pz9x1c4URUVuwTL62vJZdB8QZzvrLJF4i<br/>
![1Pz9x1c4URUVuwTL62vJZdB8QZzvrLJF4i](http://1m5.io/images/donations/1Pz9x1c4URUVuwTL62vJZdB8QZzvrLJF4i.png)

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
mesh networks and they can communicate directly device-to-device using technologies such as WiFi Direct. Firechat is an
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
- Decentralized applications and cryptocurrencies like Bitcoin are helping to wrestle some control from centralized organizations although they are largely used on servers and distributed ledgers are still logically centralized and difficult to maintain anonymity at the network layer.
- Smartphone ownership around the world is greater than PC ownership.
- Smartphones, our primary means of global communication and collaboration, are weak in maintaining our anonymity and privacy - critical to ensuring individual freedom.

## Solution
1M5 works to solve these issues by providing three primary benefits.

1. Intelligent Censorship-Resistant Anonymous Router embedding Tor, I2P, Direct Wireless Ad-Hoc Networks, and other
networks, using them intelligently as one dynamic censorship-resistant, end-to-end encrypted, anonymous mesh network.
2. Offers access to commonly desired decentralized services in an anonymous fashion including
self-sovereign decentralized identities (DID), Bitcoin, and other privacy preserving services.
3. Provides easy to use APIs for developers to embed in their applications to facilitate up-take. 

### Routing
All requests for services, e.g. Bitcoin, require an Envelope with a sensitivity level set. This sensitivity level decides
what base level of privacy is desired. Options are None, Low, Medium, High, Very High, Extreme, and Neo.
All P2P communications use I2P as the default with latency expectations between 200 milliseconds and 2 seconds.
This is the default setting in the Envelope. When making web requests, remember to set the appropriate
sensitivity level otherwise all web requests will use the HIGH sensitivity level routing all requests through the I2P layer.

We provide a Maneuvering Condition status to signal what level of maneuvering is likely required to prevent censorship.
The sensitivity level in the Evelope is where you start while the MANCON is determined by blocks encountered during
routing and thus how to ratchet up resistance as these blocks occur.

#### NONE - MANCON 5
This setting means no requirements are desired even including SSL.

* Web: HTTPS will be tried and if fails, will attempt HTTP if the URL is HTTP. If that fails, the request will be forwarded 
to other peers until a peer can make the request returning the result directly back to the requesting peer.

#### LOW - MANCON 5
Open/normal SSL based communications with no expected censorship or privacy intrusion attempts.

* Web: will use HTTPS. Failures will not attempt HTTP but will use other peers to assist.
* Tor for .onion addresses
* I2P for .i2p addresses
* I2P is used for peer-to-peer services such as messaging

#### MEDIUM - MANCON 4
Normal censorship attempts by states on reading news (public web sites getting blocked, government shutdown of cloud cdn content).
When an HTTPS clearnet site gets blocked that has an associated Tor hidden service, that Tor hidden service will be used.
All other routing remains unchanged.

* Web: will attempt to use Tor. 
If fails and an associated Tor hidden service is available, that hidden services will be used.
If no Tor hidden service is associated with the site, other peers will be used to assist.
Expect latencies of 500 milliseconds to 2 seconds.

#### HIGH - MANCON 3
Tor hidden services that have been blocked or taken down but have an associated I2P eep site, 
that I2P eep site will be accessed.

Default sensitivity in Envelope.

* Web: will use an I2P peer that has access to Tor to make the request. 
Expect latencies of 1-4 seconds.

#### VERYHIGH - MANCON 2
I2P eep sites getting attacked/targeted. 
Use 1M5 mainly with I2P with high delays. 
Only able to access information directly via I2P using a decentralized content distribution network, e.g. Inkrypt.

* Web: will use an I2P peer with random delays that has access to Tor to make the request. 
Expect latencies of 2-3 minutes.
* P2P: direct comms with I2P but with random delays. Expect latencies of 2-90 seconds.

#### EXTREME - MANCON 1
Local cellular service towers shutdown.
Use 1M5 with 1DN to route to peers with internet access.

Wide-ranging latencies but strong privacy.

* Web: a 1DN peer will be used to access Tor. 
Expect latencies of 2-25 minutes when in large cities with many 1M5 nodes.
* P2P: 1DN peers will be used until a peer with I2P access can route the request.

#### NEO - MANCON 0
Whistleblower with deep state top secrets or strong censorship attempts 
(Tor Nodes blocking / deep packet inspections / I2P timing attacks / local cellular tower shutdowns) 
in jurisdictions with no freedom of expression protections (North Korea, China, Syria, Iran): 
1M5 with 1DN to I2P with High Delays on Purism Libre Mobile and Laptops.

Wide-ranging latencies but highest privacy.

* Web: 1DN is used to access a peer that will then request another peer using I2P with high delays to make the Tor request. 
Expect latencies of 2-30 minutes when in large cities with many 1M5 nodes.
* P2P: 1DN is used to forward a message through a random number and combination of 1DN/I2P peers at random delays of up to 90 seconds
at the I2P layer and up to 3 months at the 1M5 layer. A random number of copies (3 min 12 max) of the message are sent out.

## Design
1M5 is composed of a Service-Oriented Architecture (SOA) design using a minimalistic service bus for micro-services, 
a Staged Event-Driven Architecture (SEDA) design for asynchronous multi-threaded inter-service communications,
a service registry, internal core services, and a number of Sensors for advanced intelligent interaction with peers.

Key internal services include: 

* Enterprise Applications Integration (EAI) Routing service for providing data-driven inter-service routing patterns
* Key Chain Service with key management and encryption/decryption features

Key add-on services include:

* Sensors Service for integrating different Sensor Managers for intelligent censorship-resistant routing
* Decentralized IDentification (DID) Service for authentication/authorization and reputation building (Web of Trust)

## [Implementation](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/README.md)
The application is written in Java using Android 23 JDK 1.7 to ensure the core can run in Android 5.0+. 
Android is a subset of Java's JDK.

Documentation of the Core starts [here](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/README.md).

## Integrations

### [Inkrypt](https://inkrypt.io)

#### Anonymous Decentralized Cloud 
An anonymous decentralized Content Delivery Network (DCDN). Use of OpenPGP keys supported as well as
AES 256 encryption for content.

#### nLightn
Inkrypt is building a decentralized censorship resistant network for citizen journalists 
to store and publish articles fighting government
oppression of the right to free speech globally. They need censorship resistant identities
to protect journalists from harm yet support their ability to build a reputation as a trusting
source for news and to ensure that information is also not censored nor stolen yet allows
the journalist to release portions of the information as they desire to whom they desire
to share it with to include no one or everyone (global public).

### [Purism](https://puri.sm)

#### Chatty
Messaging over a censorship-resistant anonymous peer-to-peer network.

#### PureBrowser
Upon integrating with Chatty, we will be looking at integration in the Purism PureBrowser to ensure
anyone world-wide can access any web page anywhere in the world anonymously.

## Support
Support can be purchased with Bitcoin when availability exists. Please request support
at: info@1m5.io

Consulting is purchased in 10 hour blocks for 1 BTC (as of 1/2019).

On-demand availability can be purchased in annual packages below.
* Service Level Agreement (SLA) is maximum response time in hours
* X is multiplier: Multiply it by the number of 10 hour blocks you wish to purchase to determine price,
e.g. 30 hours of Live SLA is 2 * 30 hours * 1 BTC/10 hours = 6 BTC

When multiple SLA packages are purchased
and a support request is made, include the
SLA package you wish to use.

Note: Unused hours do not accumulate.

| Package  | SLA  |  X   |
|----------|:----:|:----:|
| Develop  |   32 |  1   |
| Testing  |   18 |  1.5 |   
| Live     |    2 |  2   |
| Critical | 0.25 |  3   |

## Fund Raising
1M5 is funded entirely through donations, support, and volunteers. 
The following are current and potential donation sources.

### [Alex Jones](https://en.wikipedia.org/wiki/Alex_Jones)
An American radio show host pushing the boundaries on free speech in the United States.

* https://www.infowars.com/

### [American Civil Liberties Union (ACLU)](https://www.aclu.org/)

* https://www.aclu.org/issues/national-security/privacy-and-surveillance/nsa-surveillance

### [Anonymous](https://en.wikipedia.org/wiki/Anonymous_(group))
A decentralized international hacktivist group. 
"We are Anonymous. We are Legion. We do not forgive. We do not forget. Expect us."
Broadly speaking, Anons oppose Internet censorship and control and the majority of their actions target governments, 
organizations, and corporations that they accuse of censorship. 

### [Antifa](https://en.wikipedia.org/wiki/Antifa_(United_States))
Movement against fascism.

### [Electronic Frontier Foundation](https://www.eff.org/)

* https://www.eff.org/issues/privacy
* https://ssd.eff.org/

### [Freedom of the Press Foundation](https://freedom.press/)
FPF assists with crowdfunding those projects aiming to improve on journalism tools aimed at security.
The problem is that they only take donations via card and PayPal which is anything but anonymous.

* https://freedom.press/crowdfunding/

### [Freedom's Phoenix](https://www.freedomsphoenix.com/)
Declare your independence with Ernest Hancock.

### [The Guardian Project](https://guardianproject.info/)
Creates easy to use secure apps, open-source software libraries, and customized mobile devices that can 
be used around the world by any person looking to protect their communications and personal data 
from unjust intrusion, interception and monitoring.

### [Inkrypt](https://www.inkrypt.io/)
Censorship-Resistant Decentralized Content Distribution Network with OpenPGP/AES Encryption and Anonymity as a base.

### [Purism](https://puri.sm/)
A security and freedom-focused computer manufacturer based in San Francisco, founded in 2014 with the 
fundamental goals of combining the philosophies of the Free Software movement with the hardware 
manufacturing process, and to make it easy for individuals and businesses to have computers that 
they can trust and feel safe using, i.e. making security more convenient than not.

### [ODD Reality](https://videofull.net/channel?id=UCuftdXePz6z73Wsg8Ao5lTg)

## Threats & Counter Measures
Censorship attempts can be made in a myriad of ways and are ever changing as technology changes and attackers grow in experience.
Below are some of these methods and how 1M5 and composite networks mitigate them.

### General Information

#### I2P
Design of I2P started in 2003 soon after TOR and Freenet came on the scene. It uses many of the features of onion routing
as in TOR but also adds some enhancements which earned its routing the name of 'garlic routing'. The I2P team has identified
threats, some specific to I2P, and mitigations. 

Resources
* [Site](https://geti2p.net/en/docs/how/threat-model)

#### TOR

#### 1DN

#### 1M5

### DNS Blocking
Inbound and outbound blocking of IP addresses by DNS servers.

Resources
* [Wiki](https://en.wikipedia.org/wiki/DNS_blocking)

#### I2P

#### TOR

#### 1DN

#### 1M5

### DNS Poisoning
Corruption in a DNS server's resolver cache by swapping out valid IP addresses with invalid addresses resulting in traffic divertion.

Resources
* [Wiki](https://en.wikipedia.org/wiki/DNS_spoofing)

#### I2P

#### TOR

#### 1DN

#### 1M5

### Brute Force
This attack is performed by watching all messages passing between all nodes attempting to correlate messages into paths.

#### I2P
Difficult attack to initiate against I2P as all peers are sending messages frequently across the network (end-to-end and for network maintenance) 
and each end-to-end message changes in size and data along its path. The attacker also does not have access to the messages
as inter-router communications are encrypted and in mutiple 'cloves' (garlic) so that two messages each 1kb in size would
appear as one 2kb message.

A bruce force attacker can induce trends by sending an unusually large payload to an I2P destination while monitoring all
network connections eliminating all nodes that didn't receive it. The cost to mount this kind of brute force attack on 
I2P would be very expensive so not a high priority.

Preventing a single router or group of routers from attempting to route all I2P traffic is ensured due to each router
placing limits on the number of tunnels that can be routed by a single peer.

#### TOR

#### 1DN

#### 1M5
The 1M5 network is slated to provide random delays across its nodes and extended persistent delays (e.g. months), 
and bandwidth throttling on streams to help combat this attack at the application layer.

### Timing
Timing attacks seek to correlate messages across the network based on their latencies combined with expected behavioral
patterns, e.g. HTTP requests receive responses.

#### I2P
I2P uses unidirectional datagrams so there are no replies although this is not the case with streaming and the guaranteed
delivery mode. 

#### TOR

#### 1DN

#### 1M5

### Intersection

#### I2P

#### TOR

#### 1DN

#### 1M5

### Tagging

#### I2P

#### TOR

#### 1DN

#### 1M5

### Partitioning

#### I2P

#### TOR

#### 1DN

#### 1M5

### Predecessor

#### I2P

#### TOR

#### 1DN

#### 1M5

### Harvesting

#### I2P

#### TOR

#### 1DN

#### 1M5

### Traffic Analysis Identification

#### I2P

#### TOR

#### 1DN

#### 1M5

### Sybil

#### I2P

#### TOR

#### 1DN

#### 1M5

### Buddy Exhaustion

#### I2P

#### TOR

#### 1DN

#### 1M5

### Cryptographic

#### I2P

#### TOR

#### 1DN

#### 1M5

### Floodfill Anonymity

#### I2P

#### TOR

#### 1DN

#### 1M5

### Central Resource

#### I2P

#### TOR

#### 1DN

#### 1M5

### Development

#### I2P

#### TOR

#### 1DN

#### 1M5

### Implementation (Bugs)

#### I2P

#### TOR

#### 1DN

#### 1M5

### Blocklists

#### I2P

#### TOR

#### 1DN

#### 1M5

### Distributed Denial of Service (DDoS)
A network-attack in which the perpetrator seeks to make a machine or network resource unavailable to its intended users 
by temporarily or indefinitely disrupting services of a networked host.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Denial-of-service_attack)

#### Greedy User

#### I2P

#### TOR

#### 1DN

#### 1M5

#### Starvation

#### I2P

#### TOR

#### 1DN

#### 1M5

#### Flooding

#### I2P

#### TOR

#### 1DN

#### 1M5

#### Ping Flood

##### I2P

##### TOR

##### 1DN

##### 1M5

#### CPU Loading

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Floodfill

##### I2P

##### TOR

##### 1DN

##### 1M5

#### ReDoS

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Twinge

##### I2P

##### TOR

##### 1DN

##### 1M5

#### SYN Flood
DDos attacks by initiating TCP/IP handshakes but either not responding with a final ACK or responding with a different IP address.

Resources
* [Wiki](https://en.wikipedia.org/wiki/SYN_flood)

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Layer 7
DDoS attacks on application-layer processes.

Resources

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Ping of Death

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Smurf Attack

Resources
* [Wiki](https://en.wikipedia.org/wiki/Smurf_attack)

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Fraggle Attack

Resources
* [Wiki](https://en.wikipedia.org/wiki/Smurf_attack#Fraggle_Attack)

##### I2P

##### TOR

##### 1DN

##### 1M5

### Advanced Persistent Threat (APT)
A stealthy computer network attack in which a person or group gains unauthorized access to a network and remains 
undetected for an extended period.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Advanced_persistent_threat)

#### I2P

#### TOR

#### 1DN

#### 1M5

### Advanced Volatile Threat (AVT)
A stealthy computer network attack in which a person or group gains unauthorized access to a network and remains 
undetected in memory never persisting to the hard-drive circumventing investigative techniques.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Advanced_volatile_threat)

#### I2P

#### TOR

#### 1DN

#### 1M5
