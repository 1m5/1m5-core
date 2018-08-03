package io.onemfive.core.keyring.fileencryption;

import io.onemfive.core.Util;

/**
 * Contains a symmetric encryption key derived from a password,
 * and the parameters involved in deriving the key (i.e. salt and
 * <code>scrypt</code> parameters).
 *
 * Originally from I2P-Bote.
 */
public class DerivedKey {
    byte[] salt;
    SCryptParameters scryptParams;
    byte[] key;
    
    DerivedKey(byte[] salt, SCryptParameters scryptParams, byte[] key) {
        this.salt = salt;
        this.scryptParams = scryptParams;
        this.key = key;
    }
    
    void clear() {
        Util.zeroOut(salt);
        Util.zeroOut(key);
    }
    
    /** Makes a deep copy */
    @Override
    public DerivedKey clone() {
        return new DerivedKey(salt.clone(), scryptParams, key.clone());
    }
}