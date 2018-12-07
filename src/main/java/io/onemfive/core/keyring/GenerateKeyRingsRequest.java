package io.onemfive.core.keyring;

public class GenerateKeyRingsRequest extends KeyRingsRequest {
    public static int KEYRING_USERNAME_REQUIRED = 2;
    public static int KEYRING_PASSPHRASE_REQUIRED = 3;
    public static int ALIAS_REQUIRED = 4;
    public static int ALIAS_PASSPHRASE_REQUIRED = 5;

    public String keyRingUsername;
    public String keyRingPassphrase;
    public String alias;
    public String aliasPassphrase;
    public int hashStrength = KeyRingService.PASSWORD_HASH_STRENGTH_64;
}
