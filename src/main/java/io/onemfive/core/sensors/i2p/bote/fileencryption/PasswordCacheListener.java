package io.onemfive.core.sensors.i2p.bote.fileencryption;

public interface PasswordCacheListener {

    /**
     * Called after the user enters a password (which may or may not be correct),
     * but before any encrypted files are accessed.
     */
    void passwordProvided();

    /** Called when the user chooses to clear the password, or the cache expires. */
    void passwordCleared();
}
