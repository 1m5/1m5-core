package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class LoadKeyRingRequest extends ServiceRequest {
    public String alias;
    public char[] passphrase;
    public String algorithm;
    public String location;
}
