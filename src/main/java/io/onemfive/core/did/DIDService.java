package io.onemfive.core.did;

import io.onemfive.core.*;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Decentralized IDentifier (DID) Service
 *
 * @author objectorange
 */
public class DIDService extends BaseService {

    private static final Logger LOG = Logger.getLogger(DIDService.class.getName());

    public static final String OPERATION_VERIFY = "VERIFY";
    public static final String OPERATION_AUTHENTICATE = "AUTHENTICATE";
    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_AUTHENTICATE_CREATE = "AUTHENTICATE_CREATE";
    public static final String OPERATION_HASH = "HASH";
    public static final String OPERATION_VERIFY_HASH = "VERIFY_HASH";

    private Map<String,DID> contacts;

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
            case OPERATION_AUTHENTICATE_CREATE: {authenticateOrCreate(e);break;}
            case OPERATION_HASH: {
                HashRequest r = (HashRequest)DLC.getData(HashRequest.class,e);
                hash(r);
                break;
            }
            case OPERATION_VERIFY_HASH:{
                VerifyHashRequest r = (VerifyHashRequest)DLC.getData(VerifyHashRequest.class,e);
                verifyHash(r);
                break;
            }
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
     * Creates and returns identity key using master key for provided alias if one does not exist.
     * If master key is not present, one will be created by the Key Ring Service.
     * @param e
     */
    private void create(Envelope e) {
        LOG.info("Received create DID request.");
        DID did = e.getDID();
        // make sure we don't already have a key
        if(contacts.get(did.getAlias()) == null) {

        }
//        DID didCreated = infoVault.getDidDAO().createDID(did.getAlias(), did.getPassphrase());
//        didCreated.setAuthenticated(true);
//        e.setDID(didCreated);
    }

    /**
     * Authenticates passphrase
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
    }

    private void authenticateOrCreate(Envelope e) {
        verify(e);
        if(!e.getDID().getVerified()) {
            create(e);
        } else {
            authenticate(e);
        }
    }

    private void hash(HashRequest r) {
        try {
            MessageDigest md = MessageDigest.getInstance(r.hashAlgorithm);
            r.hash = md.digest(r.contentToHash);
        } catch (NoSuchAlgorithmException e1) {
            r.exception = e1;
        }
    }

    private void verifyHash(VerifyHashRequest r) {
        HashRequest hr = new HashRequest();
        hr.contentToHash = r.content;
        hr.hashAlgorithm = r.hashAlgorithm;
        hash(hr);
        r.isAMatch = hr.exception == null && hr.hash != null && new String(hr.hash).equals(new String(r.hashToVerify));
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);

        contacts = new HashMap<>();

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down....");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
