package io.onemfive.core.sensors.i2p.bote.crypto;

import java.security.PrivateKey;

/**
 * Holds a private encryption key and a private signing key.
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
