package io.onemfive.core.keyring;

import io.onemfive.data.PublicKey;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class AuthNRequest extends KeyRingsRequest {

    public static int KEY_RING_USERNAME_REQUIRED = 2;
    public static int KEY_RING_PASSPHRASE_REQUIRED = 3;
    public static int ALIAS_REQUIRED = 4;
    public static int ALIAS_PASSPHRASE_REQUIRED = 5;
    public static int ALIAS_UNKNOWN = 6;
    public static int KEYRING_LOCATION_REQUIRED = 7;
    public static int KEYRING_LOCATION_INACCESSIBLE = 8;

    // Request
    public String location;
    public String keyRingUsername;
    public String keyRingPassphrase;
    public String alias;
    public String aliasPassphrase;
    public boolean autoGenerate = false;

    // Response
    public PublicKey identityPublicKey;
    public PublicKey encryptionPublicKey;
}
