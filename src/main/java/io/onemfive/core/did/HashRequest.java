package io.onemfive.core.did;

import io.onemfive.core.ServiceRequest;

public class HashRequest extends ServiceRequest {
    public byte[] contentToHash;
    public String hashAlgorithm;
    public byte[] hash;
}
