package io.onemfive.core.keyring;

import io.onemfive.core.*;
import io.onemfive.data.Envelope;
import io.onemfive.data.EventMessage;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.security.Provider;
import java.security.Security;
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

    private static final Logger LOG = Logger.getLogger(KeyRingService.class.getName());

    private Properties properties;

    // Master Key Ring
    private PGPSecretKeyRing secretKeyRing;
    // Identity Keys


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
                StoreKeyRingRequest r = (StoreKeyRingRequest)DLC.getData(StoreKeyRingRequest.class,e);
                store(r);
            }
            case OPERATION_GET_KEYS: {
                GetKeysRequest r = (GetKeysRequest)DLC.getData(GetKeysRequest.class,e);
                getKeys(r);
            }
            default: deadLetter(e);
        }
    }

    private void store(StoreKeyRingRequest r) {

    }

    private void load(LoadKeyRingRequest r) {

    }

    private void getKeys(GetKeysRequest r) {

    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        this.properties = properties;
        try {
            Config.loadFromClasspath("keyring.config", properties, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        Security.addProvider(new BouncyCastleProvider());


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
        Security.addProvider(new BouncyCastleProvider());

    }

}
