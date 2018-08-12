package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class GenerateKeyPairRequest extends ServiceRequest {
    public static int ALIAS_REQUIRED = 1;
    public static int PASSPHRASE_REQUIRED = 2;

    public String alias;
    public char[] passphrase;
    public int hashStrength = KeyRingService.PASSWORD_HASH_STRENGTH_64;
}
