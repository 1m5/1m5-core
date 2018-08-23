package io.onemfive.core.did.crypto;

import io.onemfive.core.util.fileencryption.PasswordException;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Called when a private key in an Email Identity was changed and
 * needs to be written back to the identities file.
 *
 * Originally from I2P-Bote.
 */
public interface KeyUpdateHandler {
    
    void updateKey() throws GeneralSecurityException, PasswordException, IOException;
}