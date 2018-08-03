package io.onemfive.core.did.crypto;

import java.security.PrivateKey;

/**
 * Holds a private encryption key and a private signing key.
 *
 * Originally from I2P-Bote.
 *
 * @see java.security.KeyPair
 */
public class PrivateKeyPair {
    public PrivateKey encryptionKey;
    public PrivateKey signingKey;
    
    public PrivateKeyPair() {
    }
    
    public PrivateKeyPair(PrivateKey encryptionKey, PrivateKey signingKey) {
        this.encryptionKey = encryptionKey;
        this.signingKey = signingKey;
    }
}