package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class DecryptRequest extends ServiceRequest {
    public byte[] encryptedContent;
    public String alias;
    public char[] passphrase;
    // Response
    public byte[] plaintextContent;
}
