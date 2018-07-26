package io.onemfive.core.did;

import io.onemfive.core.ServiceRequest;

public class VerifyHashRequest extends ServiceRequest {
    public byte[] content;
    public byte[] hashToVerify;
    public String hashAlgorithm;
    public boolean isAMatch;
}
