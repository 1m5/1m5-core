package io.onemfive.core.sensors.i2p.bote.crypto;

import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.i2p.util.Log;

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

    private static List<CryptoImplementation> instances;

    public synchronized static CryptoImplementation getInstance(int id) {
        if (instances == null)
            init();

        for (CryptoImplementation instance: instances)
            if (instance.getId() == id)
                return instance;
        return null;
    }

    public synchronized static List<CryptoImplementation> getInstances() {
        if (instances == null)
            init();

        return instances;
    }

    private static void init() {
        instances = Collections.synchronizedList(new ArrayList<CryptoImplementation>());
        Log log = new Log(CryptoFactory.class);
        try {
            instances.add(new ElGamal2048_DSA1024());
        } catch (GeneralSecurityException e) {
            log.error("Error creating ElGamal2048_DSA1024.", e);
        }
        try {
            instances.add(new ECDH256_ECDSA256());
            instances.add(new ECDH521_ECDSA521());
        }
        catch (GeneralSecurityException e) {
            log.error("Error creating ECDH256_ECDSA256 or ECDH521_ECDSA521.", e);
        }
        try {
            instances.add(new NTRUEncrypt1087_GMSS512());
        } catch (GeneralSecurityException e) {
            log.error("Error creating NTRUEncrypt1087_GMSS512.", e);
        }
    }
}
