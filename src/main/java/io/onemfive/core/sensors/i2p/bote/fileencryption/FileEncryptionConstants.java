package io.onemfive.core.sensors.i2p.bote.fileencryption;

import java.util.Random;

public class FileEncryptionConstants {
    public static final byte[] START_OF_FILE = "IBef".getBytes();   // "I2P-Bote encrypted file"
    static final int FORMAT_VERSION = 1;   // file format identifier
    static final int KEY_LENGTH = 32;   // encryption key length
    static final int SALT_LENGTH = 32;
    static final SCryptParameters KDF_PARAMETERS = new SCryptParameters(1<<14, 8, 1);
    static final int BLOCK_SIZE = 16;   // length of the AES initialization vector; also the AES block size for padding. Not to be confused with the AES key size.
    static final byte[] PASSWORD_FILE_PLAIN_TEXT = "If this is the decrypted text, the password was correct.".getBytes();
    static final byte[] DEFAULT_PASSWORD;   // this is substituted for empty passwords to add some security through obscurity,
    // and because empty passwords don't work with scrypt (see FileEncryptionUtil.getEncryptionKey())
    static {
        Random rng = new Random(999);
        DEFAULT_PASSWORD = new byte[10];
        rng.nextBytes(DEFAULT_PASSWORD);
    }
}
