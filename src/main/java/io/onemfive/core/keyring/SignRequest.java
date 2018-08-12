package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class SignRequest extends ServiceRequest {
    public static int CONTENT_TO_SIGN_REQUIRED = 1;
    public static int ALIAS_REQUIRED = 2;
    public static int PASSPHRASE_REQUIRED = 3;
    public static int SECRET_KEY_NOT_FOUND = 4;

    public byte[] contentToSign;
    public String alias;
    public char[] passphrase;
    // Respones
    public byte[] signature;
}
