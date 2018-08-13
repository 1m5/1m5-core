package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;
import org.bouncycastle.openpgp.PGPPublicKey;

public class GetPublicKeyRequest extends ServiceRequest {
    public static int ALIAS_OR_FINGERPRINT_REQUIRED = 1;
    public boolean master = true;
    // Alias used to retrieve master public key
    public String alias;
    // Fingerprint used to retrieve sub public key
    public byte[] fingerprint;
    // Response
    PGPPublicKey publicKey;
}
