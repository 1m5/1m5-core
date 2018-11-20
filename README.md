# 1M5 Core
A secure open-source decentralized peer-to-peer application platform with end-to-end encryption 
and anonymity as a base layer for creating easy to build and use secure decentralized peer-to-peer 
applications requiring no server connections that can be used around the world by any person looking 
to protect their communications and personal data from unethical monitoring, interception, intrusion, 
and censorship.

## Version
0.5.2

## Authors / Developers
* Founder / Lead Developer: Brian Taylor - [GitHub](https://github.com/objectorange) | [LinkedIn](https://www.linkedin.com/in/decentralizationarchitect/)
* Fund Raising: Brad Neiger - [LinkedIn](https://www.linkedin.com/in/bradneiger/)
* End User Advocate: Theresa Augustin - [LinkedIn](https://www.linkedin.com/in/theresaaaugustin/)

## Bounties
* Paid in Bitcoin

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
- Blockchain based applications and cryptocurrencies like Bitcoin are helping to wrestle some control from centralized organizations although they are largely used on servers and distributed ledgers are still logically centralized and difficult to maintain anonymity at the network layer.
- Smartphone ownership around the world is greater than PC ownership.
- Smartphones, our primary means of global communication and collaboration, are weak in maintaining our anonymity and privacy - critical to ensuring individual freedom.

## Solution
1M5 works to solve these issues by providing an intelligent router embedding Tor, I2P, Direct Wireless Mesh, and other
networks, using them intelligently as one dynamic network, and providing easy to use APIs for developers to embed in their
applications. In addition, it provides access to commonly desired decentralized services in an anonymous fashion including
self-sovereign identities, GitLab, Monero, and other privacy preserving services in the future making integration a snap.

We provide a Maneuver Condition status to signal what level of maneuvering is required to prevent censorship. They are:

* **MANCON 5**: No Security - open/normal SSL based communications with no expected censorship or privacy intrusion attempts.
* **MANCON 4**: Low Security - normal censorship attempts by states on reading news (public web sites getting blocked, government shutdown of cloud cdn content): Offer Tor hidden services so that people can still access information
* **MANCON 3**: Medium Security - Tor hidden services discovered with targeted takedowns. I2P hidden services for those few who know how to access those on all devices.
* **MANCON 2**: Medium-High Security - I2P hidden services getting attacked/targeted. Use 1M5 mainly with I2P.
* **MANCON 1**: Highest of Security - whistleblower with deep state top secrets or strong censorship attempts (Tor Nodes blocking / deep packet inspections / I2P timing attacks) in jurisdictions with no freedom of expression protections (North Korea, China, Syria, Iran): 1M5 with I2P Bote/1DM on Purism Libre Mobile and Laptops.

## Threats & Counter Measures
Censorship attempts can be made in a myriad of ways and are ever changing as technology changes and attackers grow in experience.
Below are some of these methods and how 1M5 and composite networks mitigate them.

### DNS Blocking
Inbound and outbound blocking of IP addresses by DNS servers.

Resources
* [Wiki](https://en.wikipedia.org/wiki/DNS_blocking)

### DNS Poisoning
Corruption in a DNS server's resolver cache by swapping out valid IP addresses with invalid addresses resulting in traffic divertion.

Resources
* [Wiki](https://en.wikipedia.org/wiki/DNS_spoofing)

### Brute Force

#### I2P

### Intersection

#### I2P

### Tagging

#### I2P

### Partitioning

#### I2P

### Predecessor

#### I2P

### Harvesting

#### I2P

### Traffic Analysis Identification

#### I2P

### Sybil

#### I2P

### Buddy Exhaustion

#### I2P

### Cryptographic

#### I2P

### Floodfill Anonymity

#### I2P

### Central Resource

#### I2P

### Development

#### I2P

### Implementation (Bugs)

#### I2P

### Blocklists

#### I2P

### Distributed Denial of Service (DDoS)
A network-attack in which the perpetrator seeks to make a machine or network resource unavailable to its intended users 
by temporarily or indefinitely disrupting services of a networked host.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Denial-of-service_attack)

#### Greedy User

##### I2P

#### Starvation

##### I2P

#### Flooding

##### I2P

#### Ping Flood

#### CPU Loading

##### I2P   

#### Floodfill

##### I2P

#### ReDoS

#### Twinge

#### SYN Flood
DDos attacks by initiating TCP/IP handshakes but either not responding with a final ACK or responding with a different IP address.

Resources
* [Wiki](https://en.wikipedia.org/wiki/SYN_flood)

#### Layer 7
DDoS attacks on application-layer processes.

Resources

#### Ping of Death

#### Smurf Attack

Resources
* [Wiki](https://en.wikipedia.org/wiki/Smurf_attack)

#### Fraggle Attack

Resources
* [Wiki](https://en.wikipedia.org/wiki/Smurf_attack#Fraggle_Attack)

### Advanced Persistent Threat (APT)
A stealthy computer network attack in which a person or group gains unauthorized access to a network and remains 
undetected for an extended period.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Advanced_persistent_threat)

### Advanced Volatile Threat (AVT)
A stealthy computer network attack in which a person or group gains unauthorized access to a network and remains 
undetected in memory never persisting to the hard-drive circumventing investigative techniques.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Advanced_volatile_threat)

## Design
1M5 is composed of a Service-Oriented Architecture (SOA) design using a minimalistic service bus for micro-services, 
a Staged Event-Driven Architecture (SEDA) design for asynchronous multi-threaded inter-service communications,
a service registry, internal core services, and a number of Sensors for advanced intelligent interaction with peers.

## [Implementation](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/README.md)
The application is written in Java using Android 23 JDK 1.7 to ensure the core can run in Android 5.0+. 
Android is a subset of Java's JDK.

Documentation of the Core starts [here](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/README.md).

## Integrations

### [Inkrypt](https://inkrypt.io)

#### Anonymous Decentralized Cloud 
An anonymous decentralized Content Delivery Network (DCDN).

#### nLightn
Inkrypt is building a decentralized censorship resistant network for citizen journalists 
to store and publish articles fighting government
oppression of the right to free speech globally. They need censorship resistant identities
to protect journalists from harm yet support their ability to build a reputation as a trusting
source for news and to ensure that information is also not censored nor stolen yet allows
the journalist to release portions of the information as they desire to whom they desire
to share it with to include no one or everyone (global public).

## Fund Raising
1M5 is funded entirely through donations and volunteers. The following are current and potential donation sources.

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

### [The Guardian Project](https://guardianproject.info/)
Creates easy to use secure apps, open-source software libraries, and customized mobile devices that can 
be used around the world by any person looking to protect their communications and personal data 
from unjust intrusion, interception and monitoring.

### [Inkrypt](https://www.inkrypt.io/)

### [Purism](https://puri.sm/)
A security and freedom-focused computer manufacturer based in San Francisco, founded in 2014 with the 
fundamental goals of combining the philosophies of the Free Software movement with the hardware 
manufacturing process, and to make it easy for individuals and businesses to have computers that 
they can trust and feel safe using, i.e. making security more convenient than not.

### [ODD Reality](https://videofull.net/channel?id=UCuftdXePz6z73Wsg8Ao5lTg)