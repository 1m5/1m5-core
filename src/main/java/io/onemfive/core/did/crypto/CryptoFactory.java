package io.onemfive.core.did.crypto;

import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * Originally from I2P-Bote.
 */
public class CryptoFactory {

    static {
        if (Security.getProvider("BC") == null) {
            try {
                Class<?> cls = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Constructor<?> con = cls.getConstructor(new Class[0]);
                Provider bc = (Provider)con.newInstance(new Object[0]);
                Security.addProvider(bc);
            } catch (Exception e) {
            }
        }
    }

    private static final Logger LOG = Logger.getLogger(CryptoFactory.class.getName());

    private static List<Crypto> instances;

    public synchronized static Crypto getInstance(int id) {
        if (instances == null)
            init();

        for (Crypto instance: instances)
            if (instance.getId() == id)
                return instance;
        return null;
    }

    public synchronized static List<Crypto> getInstances() {
        if (instances == null)
            init();

        return instances;
    }

    private static void init() {
        instances = Collections.synchronizedList(new ArrayList<Crypto>());

        try {
            instances.add(new ElGamal2048_DSA1024());
        } catch (GeneralSecurityException e) {
            LOG.warning("Error creating ElGamal2048_DSA1024: "+e.getLocalizedMessage());
        }
        try {
            instances.add(new ECDH256_ECDSA256());
            instances.add(new ECDH521_ECDSA521());
        }
        catch (GeneralSecurityException e) {
            LOG.warning("Error creating ECDH256_ECDSA256 or ECDH521_ECDSA521: "+e.getLocalizedMessage());
        }
        try {
            instances.add(new NTRUEncrypt1087_GMSS512());
        } catch (GeneralSecurityException e) {
            LOG.warning("Error creating NTRUEncrypt1087_GMSS512: " + e.getLocalizedMessage());
        }
    }
}
