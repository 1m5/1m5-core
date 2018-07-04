package io.onemfive.core.did;

import io.onemfive.core.*;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.DID;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Decentralized IDentifier Service
 *
 * Manages Identities
 *
 * @author objectorange
 */
public class DIDService extends BaseService {

    private static final Logger LOG = Logger.getLogger(DIDService.class.getName());

    public static final String OPERATION_VERIFY = "Verify";
    public static final String OPERATION_AUTHENTICATE = "Authenticate";
    public static final String OPERATION_CREATE = "Create";
    public static final String OPERATION_LOAD = "Load";

    public DIDService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleEvent(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleHeaders(Envelope e) {
        handleAll(e);
    }

    private void handleAll(Envelope e) {
        Route route = e.getRoute();
        switch(route.getOperation()) {
            case OPERATION_VERIFY: {verify(e);break;}
            case OPERATION_AUTHENTICATE: {authenticate(e);break;}
            case OPERATION_CREATE: {create(e);break;}
            case OPERATION_LOAD: {load(e);break;}
            default: deadLetter(e); // Operation not supported
        }
    }

    private void verify(Envelope e) {
        LOG.info("Received verify DID request.");
        DID did = e.getDID();
        DID didLoaded = infoVault.getDidDAO().load(did.getAlias());
        if(didLoaded != null && did.getAlias() != null && did.getAlias().equals(didLoaded.getAlias())) {
            didLoaded.setVerified(true);
            e.setDID(didLoaded);
            LOG.info("DID verification successful.");
        } else {
            did.setVerified(false);
            LOG.info("DID verification unsuccessful.");
        }
    }

    /**
     * Creates and returns DID
     * TODO: Create PGP master keys if not present and I2P & Bote keys if not present
     * @param e
     */
    private void create(Envelope e) {
        LOG.info("Received create DID request.");
        DID did = e.getDID();
        DID didCreated = infoVault.getDidDAO().createDID(did.getAlias(), did.getPassphrase());
        e.setDID(didCreated);
        // TODO: Create PGP master keys if not present and I2P keys if not present
//        boolean created = false;
        // Use passphrase to encrypt and cache it
//        try {
//            I2PBote.getInstance().changePassword(alias.getBytes(), passphrase.getBytes(), passphrase2.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } catch (PasswordException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * Authenticates passphrase
     * TODO: Use PGP for authentication of master keys
     * @param e
     */
    private void authenticate(Envelope e) {
        LOG.info("Received authn DID request.");
        DID did = e.getDID();
        DID didLoaded = infoVault.getDidDAO().load(did.getAlias());
        // TODO: Replace with I2PBote example below
        if(didLoaded != null && did.getAlias() != null && did.getAlias().equals(didLoaded.getAlias())
                && did.getPassphrase() != null && did.getPassphrase().equals(didLoaded.getPassphrase())) {
            didLoaded.setAuthenticated(true);
            e.setDID(didLoaded);
        } else {
            did.setAuthenticated(false);
        }
//        boolean authn = false;
//        try {
//            I2PBote.getInstance().tryPassword(passphrase.getBytes());
//            authn = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } catch (PasswordException e) {
//            e.printStackTrace();
//        }
    }

    private void load(Envelope e) {
        LOG.info("Ensure I2P identities are present in DID. Request them from Sensor Service using Very High Sensitivity.");
        // Very High Sensitivity selects I2P by default
        e.setSensitivity(Envelope.Sensitivity.VERYHIGH);
        DLC.addRoute(SensorsService.class, SensorsService.OPERATION_GET_KEYS,e);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

}
