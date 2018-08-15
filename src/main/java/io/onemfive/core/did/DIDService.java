package io.onemfive.core.did;

import io.onemfive.core.*;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final String OPERATION_GET_LOCAL_DID = "GET_LOCAL_DID";
    public static final String OPERATION_ADD_CONTACT = "ADD_CONTACT";
    public static final String OPERATION_GET_CONTACT = "GET_CONTACT";

    private static final Pattern layout = Pattern.compile("\\$31\\$(\\d\\d?)\\$(.{43})");

    private DID localDefaultDID;
    private Map<String,DID> localUserDIDs = new HashMap<>();
    private Map<String,DID> contacts = new HashMap<>();

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
            case OPERATION_GET_LOCAL_DID: {
                LOG.info("Received get DID request.");
                GetLocalDIDRequest r = (GetLocalDIDRequest)DLC.getData(GetLocalDIDRequest.class,e);
                if(r == null) {
                    r = new GetLocalDIDRequest();
                    r.errorCode = GetLocalDIDRequest.REQUEST_REQUIRED;
                    DLC.addData(GetLocalDIDRequest.class,r,e);
                    break;
                }
                if(r.did == null) {
                    r.errorCode = GetLocalDIDRequest.DID_REQUIRED;
                    break;
                }
                if(r.did.getAlias() == null) {
                    r.errorCode = GetLocalDIDRequest.DID_ALIAS_REQUIRED;
                    break;
                }
                try {
                    r.did = getLocalDID(r);
                } catch (NoSuchAlgorithmException e1) {
                    r.errorCode = GetLocalDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
                    break;
                } catch (InvalidKeySpecException e1) {
                    r.errorCode = GetLocalDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
                    LOG.warning(e1.getLocalizedMessage());
                    break;
                }
                break;
            }
            case OPERATION_ADD_CONTACT: {addContact(e);break;}
            case OPERATION_GET_CONTACT: {getContact(e);break;}
            case OPERATION_VERIFY: {
                verify(e.getDID());
                break;
            }
            case OPERATION_AUTHENTICATE: {
                LOG.info("Received authn DID request.");
                AuthenticateDIDRequest r = (AuthenticateDIDRequest)DLC.getData(AuthenticateDIDRequest.class,e);
                if(r == null) {
                    r = new AuthenticateDIDRequest();
                    r.errorCode = AuthenticateDIDRequest.REQUEST_REQUIRED;
                    DLC.addData(AuthenticateDIDRequest.class,r,e);
                    break;
                }
                if(r.did == null) {
                    r.errorCode = AuthenticateDIDRequest.DID_REQUIRED;
                    break;
                }
                if(r.did.getAlias() == null) {
                    r.errorCode = AuthenticateDIDRequest.DID_ALIAS_REQUIRED;
                    break;
                }
                if(r.did.getPassphrase() == null) {
                    r.errorCode = AuthenticateDIDRequest.DID_PASSPHRASE_REQUIRED;
                    break;
                }
                try {
                    authenticate(r);
                } catch (NoSuchAlgorithmException e1) {
                    r.errorCode = AuthenticateDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
                    break;
                } catch (InvalidKeySpecException e1) {
                    r.errorCode = AuthenticateDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
                    LOG.warning(e1.getLocalizedMessage());
                    break;
                }
                break;
            }
            case OPERATION_CREATE: {
                DID did = (DID)DLC.getData(DID.class,e);
                try {
                    create(did.getAlias(), did.getPassphrase(), did.getPassphraseHashAlgorithm());
                } catch (NoSuchAlgorithmException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (InvalidKeySpecException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                }
                break;
            }
            case OPERATION_AUTHENTICATE_CREATE: {
                AuthenticateDIDRequest r = (AuthenticateDIDRequest)DLC.getData(AuthenticateDIDRequest.class,e);
                try {
                    authenticateOrCreate(r);
                } catch (NoSuchAlgorithmException e1) {
                    r.errorCode = AuthenticateDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
                    break;
                } catch (InvalidKeySpecException e1) {
                    r.errorCode = AuthenticateDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
                    LOG.warning(e1.getLocalizedMessage());
                    break;
                }
                break;
            }
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

    private DID getLocalDID(GetLocalDIDRequest r) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if(localUserDIDs.containsKey(r.did.getAlias()))
            return localUserDIDs.get(r.did.getAlias());
        if(r.did.getPassphrase() == null) {
            r.errorCode = GetLocalDIDRequest.DID_PASSPHRASE_REQUIRED;
            return r.did;
        }
        if(r.did.getPassphraseHashAlgorithm() == null) {
            r.errorCode = GetLocalDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
            return r.did;
        }
        return create(r.did.getAlias(), r.did.getPassphrase(), r.did.getPassphraseHashAlgorithm());
    }

    private void addContact(Envelope e) {

    }

    private void getContact(Envelope e) {

    }

    private DID verify(DID did) {
        LOG.info("Received verify DID request.");
        DID didLoaded = infoVault.getDidDAO().load(did.getAlias());
        if(didLoaded != null && did.getAlias() != null && did.getAlias().equals(didLoaded.getAlias())) {
            didLoaded.setVerified(true);
            LOG.info("DID verification successful.");
        } else {
            did.setVerified(false);
            LOG.info("DID verification unsuccessful.");
        }
        return didLoaded;
    }

    /**
     * Creates and returns identity key using master key for provided alias if one does not exist.
     * If master key is not present, one will be created by the Key Ring Service.
     * @param alias String
     * @param passphrase String
     * @param passphraseHashAlgorithm String
     */
    private DID create(String alias, String passphrase, String passphraseHashAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        LOG.info("Received create DID request.");
        DID did = new DID();
        did.setAlias(alias);
        did.setPassphraseHash(generateHash(passphrase, passphraseHashAlgorithm).getBytes());
        did.setAuthenticated(true);
        did.setVerified(true);
        infoVault.getDidDAO().saveDID(did);
        return did;
    }

    /**
     * Authenticates passphrase
     * @param r AuthenticateDIDRequest
     */
    private void authenticate(AuthenticateDIDRequest r) throws NoSuchAlgorithmException, InvalidKeySpecException {
        DID loadedDID = infoVault.getDidDAO().load(r.did.getAlias());
        String token = new String(loadedDID.getPassphraseHash());
        if(loadedDID.getAlias().isEmpty()) {
            r.errorCode = AuthenticateDIDRequest.DID_ALIAS_UNKNOWN;
            r.did.setAuthenticated(false);
            return;
        }
        if(!r.did.getPassphraseHashAlgorithm().equals(loadedDID.getPassphraseHashAlgorithm())) {
            r.errorCode = AuthenticateDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_MISMATCH;
            r.did.setAuthenticated(false);
            return;
        }
        Matcher m = layout.matcher(token);
        if(!m.matches()) {
            r.errorCode = AuthenticateDIDRequest.DID_TOKEN_FORMAT_MISMATCH;
            r.did.setAuthenticated(false);
            return;
        }
        int size = 128;
        int cost = 16;
        int iterations = 1 << cost;
        byte[] hash = Base64.getUrlDecoder().decode(m.group(2));
        byte[] salt = Arrays.copyOfRange(hash, 0, size / 8);
        KeySpec spec = new PBEKeySpec(r.did.getPassphrase().toCharArray(), salt, iterations, size);
        SecretKeyFactory f = SecretKeyFactory.getInstance(r.did.getPassphraseHashAlgorithm());
        byte[] check = f.generateSecret(spec).getEncoded();
        int zero = 0;
        for (int idx = 0; idx < check.length; ++idx)
            zero |= hash[salt.length + idx] ^ check[idx];
        r.did.setAuthenticated(zero == 0);
    }

    private String generateHash(String passphrase, String passphraseHashAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int size = 128;
        int cost = 16;
        int iterations = 1 << cost;
        byte[] salt = new byte[size/8];
        new SecureRandom().nextBytes(salt);
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iterations, size);
        SecretKeyFactory f = SecretKeyFactory.getInstance(passphraseHashAlgorithm);
        byte[] dk = f.generateSecret(spec).getEncoded();
        byte[] hash = new byte[salt.length + dk.length];
        System.arraycopy(salt, 0, hash, 0, salt.length);
        System.arraycopy(dk, 0, hash, salt.length, dk.length);
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        return "$31$" + cost + '$' + enc.encodeToString(hash);
    }

    private void authenticateOrCreate(AuthenticateDIDRequest r) throws NoSuchAlgorithmException, InvalidKeySpecException {
        r.did = verify(r.did);
        if(!r.did.getVerified()) {
            create(r.did.getAlias(), r.did.getPassphrase(), r.did.getPassphraseHashAlgorithm());
        } else {
            authenticate(r);
        }
    }

    private void hash(HashRequest r) {

    }

    private void verifyHash(VerifyHashRequest r) {

    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);

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
