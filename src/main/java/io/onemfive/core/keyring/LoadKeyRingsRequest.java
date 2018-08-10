package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class LoadKeyRingsRequest extends ServiceRequest {
    public String alias;
    public char[] passphrase;
    /**
     * hashStrength: a number between 0 and 0xff that controls the number of times to iterate the password
     * hash before use. More iterations are useful against offline attacks, as it takes more
     * time to check each password. The actual number of iterations is rather complex, and also
     * depends on the hash function in use. Refer to Section 3.7.1.3 in rfc4880.txt.
     * Bigger numbers give you more iterations. As a rough rule of thumb, when using SHA256 as
     * the hashing function, 0x10 gives you about 64 iterations, 0x20 about 128, 0x30 about 256
     * and so on till 0xf0, or about 1 million iterations. The maximum you can go to is 0xff,
     * or about 2 million iterations. These values are constants in the KeyRingService class as helpers.
     */
    public int hashStrength = KeyRingService.PASSWORD_HASH_STRENGTH_130k;
    public String secretKeyRingCollectionFileLocation;
    public String publicKeyRingCollectionFileLocation;
}
