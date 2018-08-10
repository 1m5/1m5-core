package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class GenerateKeyPairRequest extends ServiceRequest {
    public String alias;
    public String passphrase;
}
