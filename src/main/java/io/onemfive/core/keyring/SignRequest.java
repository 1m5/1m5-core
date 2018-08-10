package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;

public class SignRequest extends ServiceRequest {
    public byte[] contentToSign;
    public String alias;
    public char[] passphrase;
    // Respones
    public byte[] signature;
}
