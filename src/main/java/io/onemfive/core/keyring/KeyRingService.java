package io.onemfive.core.keyring;

import io.onemfive.core.*;
import io.onemfive.data.Envelope;
import io.onemfive.data.EventMessage;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Creates, persists, and removes identity keys (OpenPGP) wrapping them with a symmetric key (AES).
 * Provides identity keys for DID Service.
 * Storage currently local hard drive but slated to support external usb drives.
 *
 * Some policies:
 * <ul>
 *     <li>Confidentiality
 *          <ul>
 *              <li>No certificates will be used in 1M5 as it would require divulging an identity to an untrusted 3rd party</li>
 *          </ul>
 *     </li>
 *     <li>Availability
 *          <ul>
 *              <li>Cipher flexibility is important as 1M5 is a platform for integrating service providers and sensors</li>
 *          </ul>
 *     </li>
 * </ul>
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

    // Master Key Rings
    private PGPSecretKeyRingCollection secretKeyRingCollection;
    private PGPPublicKeyRingCollection publicKeyRingCollection;

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
            secretKeyRingCollection = new PGPSecretKeyRingCollection(fis, new JcaKeyFingerprintCalculator());
            fis = new FileInputStream(pkr);
            publicKeyRingCollection = new PGPPublicKeyRingCollection(fis, new JcaKeyFingerprintCalculator());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (PGPException ex) {
            ex.printStackTrace();
        }
    }

    private void getKeys(Envelope e) {
        GetKeysRequest r = (GetKeysRequest)DLC.getData(GetKeysRequest.class,e);

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

        if(properties.get("1m5.keyring.secretKeyRingCollectionFile") != null && properties.get("1m5.keyring.publicKeyRingCollectionFile") != null) {
            LoadKeyRingRequest r = new LoadKeyRingRequest();
            r.secretKeyRingCollectionFileLocation = properties.getProperty("1m5.keyring.secretKeyRingCollectionFile");
            r.publicKeyRingCollectionFileLocation = properties.getProperty("1m5.keyring.publicKeyRingCollectionFile");
            r.passphrase = "1234".toCharArray();
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
