package io.onemfive.core.did;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.bus.Route;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;

import java.io.IOException;
import java.util.Properties;

/**
 * Decentralized IDentifier Service
 *
 * Manages Identities
 *
 * @author objectorange
 */
public class DIDService extends BaseService {

    private static String dbFileName = ".did";
    private static Properties db;

    public DIDService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleHeaders(Envelope envelope) {
        Route route = (Route) envelope.getHeader(Envelope.ROUTE);
        switch(route.getOperation()) {
            case "Verify": verify(envelope);break;
            case "Authenticate": authenticate(envelope);break;
            case "Create": create(envelope);break;
            default: deadLetter(envelope); // Operation not supported
        }
    }

    private void verify(Envelope envelope) {
        System.out.println(DIDService.class.getSimpleName()+": Received verify DID request.");
        DID did = (DID)envelope.getHeader(Envelope.DID);
        if(did != null) {
            if(db.containsKey(did.getAlias())) {
                did.setStatus(DID.Status.valueOf(db.getProperty(did.getAlias()+".status")));
            } else {
                did.setStatus(DID.Status.UNREGISTERED);
            }
            reply(envelope);
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
        boolean created = false;
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
        Boolean createNew = true;
        // TODO: 2 and 3 not available
        Integer cryptoImplId = 1; // 1 = ElGamal2048_DSA1024, 2 = ECDH256_ECDSA256, 3 = ECDH521_ECDSA521, 4 = NTRUEncrypt1087_GMSS512
        String vanityPrefix = "";
        String key = "";
        String publicName = "";
        String description = "";
        String pictureBase64 = "";
        String emailAddress = "";
        Boolean setDefault = true;
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
        did.setStatus(DID.Status.ACTIVE);
        db.setProperty(did.getAlias()+".passphrase",did.getPassphrase());
        db.setProperty(did.getAlias()+".status",DID.Status.ACTIVE.name());
        try {
            Config.saveToBase(dbFileName, db);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reply(envelope);
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
        did.setAuthenticated(did.getPassphrase().equals(db.getProperty(did.getAlias()+".passphrase")));
        if(db.getProperty(did.getAlias()+".status") == null) {
            did.setStatus(DID.Status.UNREGISTERED);
        } else {
            did.setStatus(DID.Status.valueOf(db.getProperty(did.getAlias()+".status")));
        }
        reply(envelope);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("DIDService starting up....");
        try {
            db = Config.loadFromBase(dbFileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DIDService startup failed.");
            return false;
        }
        System.out.println("DIDService started.");
        return true;
    }

}
