package io.onemfive.core.did;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.DID;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.Properties;

/**
 * Decentralized IDentifier Service
 *
 * Manages Identities
 *
 * @author objectorange
 */
public class DIDService extends BaseService {

    public static final String OPERATION_VERIFY = "Verify";
    public static final String OPERATION_AUTHENTICATE = "Authenticate";
    public static final String OPERATION_CREATE = "Create";

    private static String dbFileName = ".did";
    private static Properties db;

    public DIDService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleHeaders(Envelope envelope) {
        Route route = (Route) envelope.getHeader(Envelope.ROUTE);
        switch(route.getOperation()) {
            case OPERATION_VERIFY: {
                verify(envelope);
                break;
            }
            case OPERATION_AUTHENTICATE: {
                authenticate(envelope);
                break;
            }
            case OPERATION_CREATE: {
                create(envelope);
                break;
            }
            default: deadLetter(envelope); // Operation not supported
        }
    }

    private void verify(Envelope envelope) {
        System.out.println(DIDService.class.getSimpleName()+": Received verify DID request.");
        DID did = (DID)envelope.getHeader(Envelope.DID);
        DID didLoaded = infoVault.getDidDAO().load(did.getAlias());
        if(didLoaded != null && did.getAlias() != null && did.getAlias().equals(didLoaded.getAlias())) {
            didLoaded.setVerified(true);
            envelope.setHeader(Envelope.DID, didLoaded);
        } else {
            did.setVerified(false);
        }
    }

    /**
     * Creates and returns DID
     * TODO: Option 1: connect to local Bote instance to create public/private keys
     * TODO: Option 2: role your own crypto for generating keys that can be used in I2P
     * @param envelope
     */
    private void create(Envelope envelope) {
        System.out.println(DIDService.class.getSimpleName()+": Received create DID request.");
        DID did = (DID)envelope.getHeader(Envelope.DID);
        DID didCreated = infoVault.getDidDAO().createDID(did.getAlias(), did.getPassphrase());
        envelope.setHeader(Envelope.DID, didCreated);
        // TODO: Implement I2PBote example by requesting from I2P Sensor
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

        // Create a default Identity
//        Boolean createNew = true;
        // TODO: 2 and 3 not available
//        Integer cryptoImplId = 1; // 1 = ElGamal2048_DSA1024, 2 = ECDH256_ECDSA256, 3 = ECDH521_ECDSA521, 4 = NTRUEncrypt1087_GMSS512
//        String vanityPrefix = "";
//        String key = "";
//        String publicName = "";
//        String description = "";
//        String pictureBase64 = "";
//        String emailAddress = "";
//        Boolean setDefault = true;
//        StatusListener<ChangeIdentityStatus> lsnr = new StatusListener<ChangeIdentityStatus>() {
//            public void updateStatus(ChangeIdentityStatus status, String... args) {
//                Log.i(LID.class.getName(),"Creating default identity; status="+status.name());
//                ArrayList<String> tmp = new ArrayList<>(Arrays.asList(args));
//                tmp.add(0, status.name());
//                publishProgress(tmp.toArray(new String[tmp.size()]));
//            }
//        };

//        try {
//            BoteHelper.createOrModifyIdentity(
//                    createNew,
//                    cryptoImplId,
//                    vanityPrefix,
//                    key,
//                    publicName,
//                    description,
//                    pictureBase64,
//                    emailAddress,
//                    new Properties(),
//                    setDefault,
//                    lsnr);
//
//            lsnr.updateStatus(ChangeIdentityStatus.SAVING_IDENTITY);
//
//            I2PBote.getInstance().getIdentities().save();
//
//            this.alias = alias;
//            this.passphrase = passphrase;
//
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } catch (PasswordException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (IllegalDestinationParametersException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Authenticates passphrase
     * TODO: Option 1: connect to local I2P Bote instance to validate passphrase
     * TODO: Option 2: role your own crypto for validating passphrases used in I2P Bote
     * @param envelope
     */
    private void authenticate(Envelope envelope) {
        System.out.println(DIDService.class.getSimpleName()+": Received authn DID request.");
        DID did = (DID)envelope.getHeader(Envelope.DID);
        DID didLoaded = infoVault.getDidDAO().load(did.getAlias());
        // TODO: Replace with I2PBote example below
        if(didLoaded != null && did.getAlias() != null && did.getAlias().equals(didLoaded.getAlias())
                && did.getPassphrase() != null && did.getPassphrase().equals(didLoaded.getPassphrase())) {
            didLoaded.setAuthenticated(true);
            envelope.setHeader(Envelope.DID, didLoaded);
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

    @Override
    public boolean start(Properties properties) {
        System.out.println(DIDService.class.getSimpleName()+": starting....");
        try {
            db = Config.loadFromBase(dbFileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(DIDService.class.getSimpleName()+": start failed.");
            return false;
        }
        System.out.println(DIDService.class.getSimpleName()+": started.");
        return true;
    }

}
