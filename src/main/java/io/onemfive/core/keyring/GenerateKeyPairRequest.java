package io.onemfive.core.keyring;

public class GenerateKeyPairRequest extends KeyRingsRequest {
    public static int ALIAS_REQUIRED = 2;
    public static int PASSPHRASE_REQUIRED = 3;

    public String alias;
    public char[] passphrase;
    public int hashStrength = KeyRingService.PASSWORD_HASH_STRENGTH_64;
}
