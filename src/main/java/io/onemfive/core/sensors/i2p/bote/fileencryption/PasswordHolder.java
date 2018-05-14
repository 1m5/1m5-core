package io.onemfive.core.sensors.i2p.bote.fileencryption;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface PasswordHolder {

    byte[] getPassword();

    DerivedKey getKey() throws IOException, GeneralSecurityException;
}
