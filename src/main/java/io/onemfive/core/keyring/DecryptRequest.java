package io.onemfive.core.keyring;

public class DecryptRequest extends KeyRingsRequest {
    public String keyRingUsername;
    public String keyRingPassphrase;
    public byte[] encryptedContent;
    public String alias;
    public String passphrase;
    // Response
    public byte[] plaintextContent;
}
