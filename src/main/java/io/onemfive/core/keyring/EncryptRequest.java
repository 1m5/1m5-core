package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class EncryptRequest extends ServiceRequest {
    public byte[] plainTextContent;
    public String alias;
    public char[] passphrase;
    // Response
    public byte[] encryptedContent;
}
