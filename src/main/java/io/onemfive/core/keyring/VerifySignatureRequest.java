package io.onemfive.core.keyring;

public class VerifySignatureRequest extends KeyRingsRequest {
    public byte[] contentSigned;
    public byte[] signature;
    public byte[] fingerprint;
    // Response
    public boolean verified = false;
}
