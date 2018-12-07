package io.onemfive.core.keyring;

public class EncryptRequest extends KeyRingsRequest {
    public static int CONTENT_TO_ENCRYPT_REQUIRED = 2;
    public static int FINGERPRINT_REQUIRED = 3;
    public static int PUBLIC_KEY_NOT_FOUND = 4;

    public String keyRingUsername;
    public String keyRingPassphrase;
    public byte[] contentToEncrypt;
    public byte[] fingerprint;
    // Response
    public byte[] encryptedContent;
}
