package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class LoadKeyRingRequest extends ServiceRequest {
    public char[] passphrase;
    public String secretKeyRingCollectionFileLocation;
    public String publicKeyRingCollectionFileLocation;
    public Boolean autoGenerate = false;
}
