package io.onemfive.core.keyring.fileencryption;

import io.onemfive.core.OneMFiveAppContext;
import net.i2p.data.SessionKey;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import static io.onemfive.core.keyring.fileencryption.FileEncryptionConstants.BLOCK_SIZE;
import static io.onemfive.core.keyring.fileencryption.FileEncryptionConstants.FORMAT_VERSION;
import static io.onemfive.core.keyring.fileencryption.FileEncryptionConstants.START_OF_FILE;


/**
 * Encrypts data with a password and writes it to an underlying {@link OutputStream}.<br/>
 * Nothing is actually written until {@link #close()} is called.<br/>
 * Nothing is written when <code>flush</code> is called, either, because if more data were
 * written after a <code>flush</code>, it would cause data corruption because the stream
 * would be encrypted two separate pieces.
 * <p/>
 * A header is written before the encrypted data. The header fields are:<br/>
 * <code>init of file, format version, scrypt parameters (N, r, p), salt, iv, encrypted data</code>.
 */
public class EncryptedOutputStream extends FilterOutputStream {
    private OutputStream downstream;
    private DerivedKey derivedKey;
    private ByteArrayOutputStream outputBuffer;
    
    /**
     * Creates an <code>EncryptedOutputStream</code> that encrypts data with a password obtained
     * from a <code>PasswordHolder</code>.
     * @throws PasswordException 
     * @throws IOException 
     * @throws GeneralSecurityException 
     */
    public EncryptedOutputStream(OutputStream downstream, PasswordHolder passwordHolder) throws PasswordException, IOException, GeneralSecurityException {
        super(downstream);
        this.downstream = downstream;
        byte[] password = passwordHolder.getPassword();
        if (password == null)
            throw new PasswordException();
        try {
            // make a copy in case PasswordCache zeros out the password before close() is called
            derivedKey = passwordHolder.getKey().clone();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } catch (InvalidKeySpecException e) {
            throw new IOException(e);
        }
        outputBuffer = new ByteArrayOutputStream();
    }
    
    public EncryptedOutputStream(OutputStream downstream, DerivedKey derivedKey) {
        super(downstream);
        this.downstream = downstream;
        this.derivedKey = derivedKey.clone();
        outputBuffer = new ByteArrayOutputStream();
    }
    
    @Override
    public void write(int b) throws IOException {
        outputBuffer.write(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) {
        outputBuffer.write(b, off, len);
    }
    
    @Override
    public void close() throws IOException {
        try {
            encryptAndWrite();
        }
        finally {
            // erase the copy of the key
            derivedKey.clear();
            derivedKey = null;
            
            downstream.close();
        }
    }
    
    /**
     * Writes the header, then encrypts the internal buffer and writes the encrypted
     * data to the underlying <code>OutputStream</code>.
     * @throws IOException
     */
    @SuppressWarnings("deprecation") // for net.i2p.crypto.AESEngine
    private void encryptAndWrite() throws IOException {
        downstream.write(START_OF_FILE);
        downstream.write(FORMAT_VERSION);
        FileEncryptionConstants.KDF_PARAMETERS.writeTo(downstream);
        
        downstream.write(derivedKey.salt);
        
        byte iv[] = new byte[BLOCK_SIZE];
        OneMFiveAppContext appContext = OneMFiveAppContext.getInstance();
        new SecureRandom().nextBytes(iv);
        downstream.write(iv);
        
        byte[] data = outputBuffer.toByteArray();
        SessionKey key = new SessionKey(derivedKey.key);
//        byte[] encryptedData = appContext.aes().safeEncrypt(data, key, iv, 0);
//        downstream.write(encryptedData);
    }
}
