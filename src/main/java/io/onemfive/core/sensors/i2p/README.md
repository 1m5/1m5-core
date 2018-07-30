# I2P Sensor
Invisible Internet Project (I2P) Sensor

## Build Notes
- Required flex-gmss-1.7p1.jar in libs folder to be added to local Maven .m2 directory:
mvn install:install-file -Dfile=flexi-gmss-1.7p1.jar -DgroupId=de.flexi -DartifactId=gmss -Dversion=1.7p1 -Dpackaging=jar
- Required certificates from the following two directories in the i2p.i2p project (I2P Router core)
to be copied to resources/io/onemfive/core/sensors/i2p/bote/certificates keeping reseed and ssl as directories:
    - /installer/resources/certificates/reseed
    - /installer/resources/certificates/ssl
    
## Applications

### [I2P Bote Sensor](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/sensors/i2p/bote/README.md)
Extends I2P Sensor embedding I2P Bote adding storable DHT for delayed routing to battle timing attacks.
Storage should only be used for Emails, not arbitrary data.
Please use InfoVault for long-term personal data and Content Service for long-term content.
    
## Attack Mitigation

- https://www.irongeek.com/i.php?page=security/i2p-identify-service-hosts-eepsites