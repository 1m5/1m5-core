package io.onemfive.core.keyring;

import org.bouncycastle.openpgp.PGPPublicKey;

public class GetPublicKeyRequest extends KeyRingsRequest {
    public static int ALIAS_OR_FINGERPRINT_REQUIRED = 2;
    public boolean master = true;
    // Alias used to retrieve master public key
    public String alias;
    // Fingerprint used to retrieve sub public key
    public byte[] fingerprint;
    // Response
    PGPPublicKey publicKey;
}
