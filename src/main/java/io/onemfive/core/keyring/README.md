# Key Ring Service
Manages keys for the bus and its user. 
Multiple users are not supported for identities.
Creates, persists, and deletes identity keys (OpenPGP). 
Wraps them with a symmetric key (AES).
Provides identity keys for DID Service.
Storage currently local hard drive but slated to support external usb drives.

## Policies
 
### Confidentiality
No certificate authorities will be used in 1M5 as it would require divulging an identity to an untrusted 3rd party.
 
### Availability
Cipher flexibility is important as 1M5 is a platform for integrating service providers and sensors.