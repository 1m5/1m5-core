package io.onemfive.core.did;

import io.onemfive.core.ServiceRequest;

public class HashRequest extends ServiceRequest {

    public static int UNKNOWN_HASH_ALGORITHM = 1;
    public static int INVALID_KEY_SPEC = 2;
    // Request
    public String contentToHash;
    public String hashAlgorithm;
    // Result
    public String hash;
}
