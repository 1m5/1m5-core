package io.onemfive.core.keyring;

public class VerifySignatureRequest extends KeyRingsRequest {

    public static int LOCATION_REQUIRED = 2;
    public static int LOCATION_INACCESSIBLE = 3;

    // Request
    public String location;
    public String keyRingUsername;
    public String keyRingPassphrase;
    public byte[] contentSigned;
    public byte[] signature;
    public byte[] fingerprint;
    // Response
    public boolean verified = false;
}
