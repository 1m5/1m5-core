package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;
import io.onemfive.data.PublicKey;

public class LoadKeyRingsRequest extends ServiceRequest {
    public static int KEY_RING_ALIAS_REQUIRED = 1;
    public static int KEY_RING_PASSPHRASE_REQUIRED = 2;
    public static int AUTOGENERATE_REMOVE_OLD_KEYS_CONFLICT = 3;
    public static int SKR_LOCATION_NOT_PROVIDED = 4;
    public static int PKR_LOCATION_NOT_PROVIDED = 5;

    // Required
    public String keyRingAlias;
    // Required
    public char[] keyRingPassphrase;
    // Required
    public String secretKeyRingCollectionFileLocation;
    // Required
    public String publicKeyRingCollectionFileLocation;

    // If publicKeyFingerprint is provided, will use it to lookup up public key.
    public String publicKeyFingerprint;
    // Otherwise if publicKeyAlias is provided, will look up public key by that.
    public String publicKeyAlias;
    // Otherwise if master is true will return the master key, if false will return the signing key.
    public boolean master = true; // default
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
    public int hashStrength = KeyRingService.PASSWORD_HASH_STRENGTH_64; // default
    public boolean autoGenerate = true; // default
    public boolean removeOldKeys = false; // default
    // Response
    public PublicKey publicKey;

}
