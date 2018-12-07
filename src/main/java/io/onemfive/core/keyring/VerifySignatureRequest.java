package io.onemfive.core.keyring;

public class VerifySignatureRequest extends KeyRingsRequest {

    public String keyRingUsername;
    public String keyRingPassphrase;
    public byte[] contentSigned;
    public byte[] signature;
    public byte[] fingerprint;
    // Response
    public boolean verified = false;
}
