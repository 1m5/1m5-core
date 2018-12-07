package io.onemfive.core.keyring;

public class SignRequest extends KeyRingsRequest {
    public static int CONTENT_TO_SIGN_REQUIRED = 2;
    public static int ALIAS_REQUIRED = 3;
    public static int PASSPHRASE_REQUIRED = 4;
    public static int SECRET_KEY_NOT_FOUND = 5;

    public String keyRingUsername;
    public String keyRingPassphrase;
    public byte[] contentToSign;
    public String alias;
    public String passphrase;
    // Respones
    public byte[] signature;
}
