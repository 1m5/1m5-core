package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class VerifySignatureRequest extends ServiceRequest {
    public byte[] contentSigned;
    public byte[] signature;
    public byte[] fingerprint;
    // Response
    public boolean verified = false;
}
