package io.onemfive.core.sensors.i2p.bote.fileencryption;

import static io.onemfive.core.sensors.i2p.bote.fileencryption.FileEncryptionConstants.BLOCK_SIZE;
import static io.onemfive.core.sensors.i2p.bote.fileencryption.FileEncryptionConstants.FORMAT_VERSION;
import static io.onemfive.core.sensors.i2p.bote.fileencryption.FileEncryptionConstants.SALT_LENGTH;
import static io.onemfive.core.sensors.i2p.bote.fileencryption.FileEncryptionConstants.START_OF_FILE;
import io.onemfive.core.sensors.i2p.bote.Util;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import net.i2p.I2PAppContext;
import net.i2p.data.SessionKey;

/**
 * Decrypts data written via {@link EncryptedOutputStream}.
 */
public class EncryptedInputStream extends FilterInputStream {
    private ByteArrayInputStream decryptedData;

    /**
     * Creates a new <code>EncryptedInputStream</code>, reads and decrypts
     * the entire underlying <code>InputStream</code>, and buffers it internally.
     * @param upstream
     * @param passwordHolder
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws PasswordException
     */
    public EncryptedInputStream(InputStream upstream, PasswordHolder passwordHolder) throws IOException, GeneralSecurityException, PasswordException {
        super(upstream);
        byte[] password = passwordHolder.getPassword();
        if (password == null)
            throw new PasswordException();

        DerivedKey cachedKey = passwordHolder.getKey();
        byte[] bytes = readInputStream(upstream, password, cachedKey);
        decryptedData = new ByteArrayInputStream(bytes);
    }

    public EncryptedInputStream(InputStream upstream, byte[] password) throws IOException, GeneralSecurityException, PasswordException {
        super(upstream);
        byte[] bytes = readInputStream(upstream, password, null);
        decryptedData = new ByteArrayInputStream(bytes);
    }

    /**
     * If <code>cachedKey</code> is not <code>null</code>, this method assumes the
     * key has been generated from a valid password.
     * @param inputStream
     * @param password
     * @param cachedKey
     * @return the decrypted data
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws PasswordException
     */
    @SuppressWarnings("deprecation") // for net.i2p.crypto.AESEngine
    private byte[] readInputStream(InputStream inputStream, byte[] password, DerivedKey cachedKey) throws IOException, GeneralSecurityException, PasswordException {
        byte[] startOfFile = new byte[START_OF_FILE.length];
        inputStream.read(startOfFile);
        if (!Arrays.equals(START_OF_FILE, startOfFile))
            throw new IOException("Invalid header bytes: " + Arrays.toString(startOfFile) + ", expected: " + Arrays.toString(START_OF_FILE));

        int format = inputStream.read();
        if (format != FORMAT_VERSION)
            throw new IOException("Invalid file format identifier: " + format + ", expected: " + FORMAT_VERSION);

        SCryptParameters scryptParams = new SCryptParameters(inputStream);
        byte[] salt = new byte[SALT_LENGTH];
        inputStream.read(salt);

        // use the cached key if it is suitable, otherwise compute the key
        byte[] keyBytes;
        if (cachedKey!=null && Arrays.equals(salt, cachedKey.salt) && scryptParams.equals(cachedKey.scryptParams))
            keyBytes = cachedKey.key;
        else
            keyBytes = FileEncryptionUtil.getEncryptionKey(password, salt, scryptParams);

        byte iv[] = new byte[BLOCK_SIZE];
        inputStream.read(iv);
        byte[] encryptedData = Util.readBytes(inputStream);

        SessionKey key = new SessionKey(keyBytes);
        I2PAppContext appContext = I2PAppContext.getGlobalContext();

        byte[] decryptedData = appContext.aes().safeDecrypt(encryptedData, key, iv);
        // null from safeDecrypt() means failure
        if (decryptedData == null)
            if (cachedKey == null)
                throw new PasswordException();
            else
                // If a derived key was supplied but decryption failed, the encrypted
                // data is corrupt or it was encrypted with a different password than
                // the key corresponds to, so don't throw a PasswordException because
                // we're assuming password and key are correct.
                throw new GeneralSecurityException("Can't decrypt using cached key.");

        return decryptedData;
    }

    @Override
    public int read() {
        return decryptedData.read();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        return decryptedData.read(b, off, len);
    }

    @Override
    public int available() {
        return decryptedData.available();
    }

    @Override
    public void mark(int readLimit) {
        decryptedData.mark(readLimit);
    }

    @Override
    public void reset() {
        decryptedData.reset();
    }
}
