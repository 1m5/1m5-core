# InfoVault Service
Secure access and management of personal information. 

## Opportunities
1M5 is designed for end users in a peer-to-peer network targeting open source
personal mobiles/tablets/laptops/desktops with the number of network nodes in the
billions. To support decentralized applications that can use this platform, 
a database is desired due the large number of nodes likely to be traversed
while supporting ACID properties yet with a tunable consistency. JanusGraph and
Cassandra are being used as inspiration but in a P2P model that can work in mobiles
over 1M5's anonymous networks such as I2P and Tor. Neo4j is an option for JVM deployments
but not Android as ART is unable to support enough of the JDK to support it (as of mid-2018).

