package io.onemfive.core.sensors.i2p.bote.crypto;

import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Called when a private key in an Email Identity was changed and
 * needs to be written back to the identities file.
 */
public interface KeyUpdateHandler {

    void updateKey() throws GeneralSecurityException, PasswordException, IOException;
}
