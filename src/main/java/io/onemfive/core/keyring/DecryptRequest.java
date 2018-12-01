package io.onemfive.core.keyring;

public class DecryptRequest extends KeyRingsRequest {
    public byte[] encryptedContent;
    public String alias;
    public char[] passphrase;
    // Response
    public byte[] plaintextContent;
}
