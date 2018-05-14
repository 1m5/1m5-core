package io.onemfive.core.sensors.i2p.bote.fileencryption;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface PasswordVerifier {

    /**
     * Tests if a password is correct and throws a <code>PasswordException</code> if it isn't.
     * @param password
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws PasswordException
     */
    void tryPassword(byte[] password) throws IOException, GeneralSecurityException, PasswordException;
}
