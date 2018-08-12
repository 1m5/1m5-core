package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class EncryptRequest extends ServiceRequest {
    public static int TEXT_TO_ENCRYPT_REQUIRED = 1;
    public static int ALIAS_REQUIRED = 2;
    public static int PUBLIC_KEY_NOT_FOUND = 3;

    public byte[] textToEncrypt;
    public String alias;
    // Response
    public byte[] encryptedContent;
}
