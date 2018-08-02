package io.onemfive.core.keyring;

import io.onemfive.core.*;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Security;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages keys for the bus and its user.
 *
 * @author ObjectOrange
 */
public class KeyRingService extends BaseService {

    public static final String OPERATION_LOAD = "LOAD";
    public static final String OPERATION_STORE = "STORE";
    public static final String OPERATION_GET_KEYS = "GET_KEYS";

    private static final String PROVIDER_BOUNCY_CASTLE = "BC";

    private static final Logger LOG = Logger.getLogger(KeyRingService.class.getName());

    private Properties properties;

    // Key Rings
    private PGPSecretKeyRing secretKeyRing;
    private PGPPublicKeyRing publicKeyRing;

    public KeyRingService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        switch (route.getOperation()) {
            case OPERATION_LOAD: {
                LoadKeyRingRequest r = (LoadKeyRingRequest)DLC.getData(LoadKeyRingRequest.class,e);
                load(r);
                break;
            }
            case OPERATION_STORE: {
                store(e);
            }
            case OPERATION_GET_KEYS: {
                getKeys(e);
            }
            default: deadLetter(e);
        }
    }

    private void store(Envelope e) {
        StoreKeyRingRequest r = (StoreKeyRingRequest)DLC.getData(StoreKeyRingRequest.class,e);

    }

    private void load(LoadKeyRingRequest r) {
        if(r.publicKeyRingCollectionFileLocation == null) {
            LOG.severe("public key collection location is required.");
            return;
        }
        if(r.secretKeyRingCollectionFileLocation == null) {
            LOG.severe("secrete key collection location is required.");
            return;
        }
        if(r.passphrase == null) {
            LOG.severe("passphrase is required.");
            return;
        }
        File skr = new File(r.secretKeyRingCollectionFileLocation);
        if(!skr.exists()) {
            try {
                if (!skr.createNewFile())
                    return;
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        File pkr = new File(r.publicKeyRingCollectionFileLocation);
        if(!pkr.exists()) {
            try {
                if (!pkr.createNewFile())
                    return;
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        try {
            FileInputStream fis = new FileInputStream(skr);
            secretKeyRing = new PGPSecretKeyRing(fis, new JcaKeyFingerprintCalculator());

            fis = new FileInputStream(pkr);
            publicKeyRing = new PGPPublicKeyRing(fis, new JcaKeyFingerprintCalculator());

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (PGPException ex) {
            ex.printStackTrace();
        }
    }

    private void getKeys(Envelope e) {
        GetKeysRequest r = (GetKeysRequest)DLC.getData(GetKeysRequest.class,e);

    }

    private PGPPublicKey getPublicKey(String alias) {
        PGPPublicKey key = null;
        if(publicKeyRing != null) {

        }
        return key;
    }

    private PGPPrivateKey getPrivateKey(String alias) {
        PGPPrivateKey key = null;

        return key;
    }

    private PGPSecretKey getSecretKey(String alias) {
        PGPSecretKey key = null;
        if(secretKeyRing != null) {

        }
        return key;
    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        try {
            properties = Config.loadFromClasspath("keyring.config", p, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        Security.addProvider(new BouncyCastleProvider());

        if(properties.get("1m5.keyring.secretKeyRingCollectionFile") != null
                && properties.get("1m5.keyring.publicKeyRingCollectionFile") != null) {
            LoadKeyRingRequest r = new LoadKeyRingRequest();
            r.secretKeyRingCollectionFileLocation = properties.getProperty("1m5.keyring.secretKeyRingCollectionFile");
            r.publicKeyRingCollectionFileLocation = properties.getProperty("1m5.keyring.publicKeyRingCollectionFile");
            r.passphrase = "x8j32o$Dz8jaf4a!iPsfa2klKdQ".toCharArray();
            r.autoGenerate = true;
            load(r);
        }

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        LOG.info("Gracefully shutting down...");
        updateStatus(ServiceStatus.GRACEFULLY_SHUTTING_DOWN);

        updateStatus(ServiceStatus.GRACEFULLY_SHUTDOWN);
        LOG.info("Gracefully Shutdown");
        return true;
    }

    public static void main(String[] args) {
        KeyRingService s = new KeyRingService(null, null);
        s.start(null);
    }

}
