package io.onemfive.core.sensors.i2p.bote.crypto;

import java.security.PublicKey;

/**
 * Holds a public encryption key and a public signing key.
 * @see java.security.KeyPair
 */
public class PublicKeyPair {
    public PublicKey encryptionKey;
    public PublicKey signingKey;

    PublicKeyPair() {
    }

    public PublicKeyPair(PublicKey encryptionKey, PublicKey signingKey) {
        this.encryptionKey = encryptionKey;
        this.signingKey = signingKey;
    }
}
