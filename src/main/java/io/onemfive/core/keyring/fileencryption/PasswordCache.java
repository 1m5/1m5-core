package io.onemfive.core.keyring.fileencryption;

import io.onemfive.core.OneMFiveConfig;
import io.onemfive.core.Util;
import io.onemfive.core.sensors.i2p.bote.Configuration;
import io.onemfive.core.util.AppThread;
import io.onemfive.core.util.SecureFileOutputStream;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static io.onemfive.core.keyring.fileencryption.FileEncryptionConstants.KDF_PARAMETERS;
import static io.onemfive.core.keyring.fileencryption.FileEncryptionConstants.SALT_LENGTH;

/**
 * Stores a password in memory so the user doesn't have to re-enter it.
 * Also caches key derivation parameters (salt and <code>scrypt</code> parameters)
 * so the key derivation function only needs to run once.
 */
public class PasswordCache extends AppThread implements PasswordHolder {

    private static Logger LOG = Logger.getLogger(PasswordCache.class.getName());

    private OneMFiveConfig configuration;
    private byte[] password;
    private DerivedKey derivedKey;
    private long lastReset;
    private Collection<PasswordCacheListener> cacheListeners;
    
    /**
     * Creates a new <code>PasswordCache</code>.
     * @param configuration
     */
    public PasswordCache(OneMFiveConfig configuration) {
        super("PasswordCache");
        this.configuration = configuration;
        cacheListeners = new ArrayList<>();
    }
    
    /**
     * Sets the password and calls <code>passwordProvided</code>
     * on all {@link PasswordCacheListener}s.
     * @param password
     */
    public synchronized void setPassword(byte[] password) {
        synchronized(this) {
            resetExpiration();
            this.password = password;
            // clear the old key
            if (derivedKey != null) {
                derivedKey.clear();
                derivedKey = null;
            }
        }
        
        for (PasswordCacheListener listener: cacheListeners)
            listener.passwordProvided();
    }
    
    /**
     * Reads salt and <code>scrypt</code> parameters from the cache file, or chooses
     * a new salt array if the file doesn't exist. The encryption key is then computed
     * and the variable <code>derivedKey</code> is populated.
     * @throws IOException 
     * @throws GeneralSecurityException 
     */
    private void createDerivedKey() throws IOException, GeneralSecurityException {
        byte[] salt = null;
        derivedKey = null;
        
        // read salt + scrypt parameters from file if available
        File derivParamFile = configuration.getKeyDerivationParametersFile();
        if (derivParamFile.exists())
            derivedKey = FileEncryptionUtil.getEncryptionKey(password, derivParamFile);
        
        // if necessary, create a new salt and key and write the derivation parameters to the cache file
        if (derivedKey==null || !derivedKey.scryptParams.equals(KDF_PARAMETERS)) {
            salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            
            DataOutputStream outputStream = null;
            try {
                byte[] key = FileEncryptionUtil.getEncryptionKey(password, salt, KDF_PARAMETERS);
                derivedKey = new DerivedKey(salt, KDF_PARAMETERS, key);
                outputStream = new DataOutputStream(new SecureFileOutputStream(derivParamFile));
                KDF_PARAMETERS.writeTo(outputStream);
                outputStream.write(salt);
            }
            finally {
                if (outputStream != null)
                    outputStream.close();
            }
        }
    }
    
    /**
     * Returns the cached password. If the password is not in the cache, the default password (if no
     * password is set) or <code>null</code> (if a password is set) is returned.
     * @return The cached password or <code>null</code> if the password is not in the cache
     */
    public synchronized byte[] getPassword() {
        resetExpiration();
        if ((password==null || password.length<=0) && !configuration.getPasswordFile().exists())
            return FileEncryptionConstants.DEFAULT_PASSWORD;
        else
            return password;
    }
    
    @Override
    public synchronized DerivedKey getKey() throws IOException, GeneralSecurityException {
        if (derivedKey == null)
            createDerivedKey();
        return derivedKey;
    }

    private void resetExpiration() {
        lastReset = System.currentTimeMillis();
    }
    
    /** Returns <code>true</code> if the password is currently cached. */
    public boolean isPasswordInCache() {
        return password != null && password.length>0;
    }
    
    /**
     * Clears the password if it is in the cache,
     * and fires {@link PasswordCacheListener}s.
     */
    public void clear() {
        synchronized(this) {
            if (password == null)
                return;
            Util.zeroOut(password);
            password = null;
            if (derivedKey != null) {
                derivedKey.clear();
                derivedKey = null;
            }
        }
        
        for (PasswordCacheListener listener: cacheListeners)
            listener.passwordCleared();
    }
    
    public void addPasswordCacheListener(PasswordCacheListener listener) {
        cacheListeners.add(listener);
    }

    public void removePasswordCacheListener(PasswordCacheListener listener) {
        cacheListeners.remove(listener);
    }
    
    /**
     * Clears the password after a certain time if {@link #getPassword()} hasn't been called.
     * @see Configuration#getPasswordCacheDuration()
     */
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
            
            try {
                long durationMilliseconds = TimeUnit.MILLISECONDS.convert(configuration.getPasswordCacheDuration(), TimeUnit.MINUTES);
                boolean isEmpty = password==null || password.length==0;
                if (System.currentTimeMillis()>lastReset+durationMilliseconds && !isEmpty)   // cache empty passwords forever
                    clear();
            } catch (RuntimeException e) {   // catch unexpected exceptions to keep the thread running
                LOG.warning("Exception caught in PasswordCache loop: " + e.getLocalizedMessage());
            }
        }
        LOG.info("PasswordCache thread exiting.");
    }
}