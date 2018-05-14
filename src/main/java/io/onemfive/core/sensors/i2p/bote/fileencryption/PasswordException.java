package io.onemfive.core.sensors.i2p.bote.fileencryption;

/**
 * This exception is thrown when a password is invalid or missing.
 */
public class PasswordException extends Exception {
    private static final long serialVersionUID = 9105407855443550137L;

    public PasswordException() {
    }

    public PasswordException(String message) {
        super(message);
    }
}
