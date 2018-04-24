package io.onemfive.core.did;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;

import java.util.Properties;

/**
 * Decentralized IDentifier Service
 *
 * @author objectorange
 */
public class DIDService extends BaseService {

    public DIDService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleHeaders(Envelope envelope) {
        String operation = (String) envelope.getHeader(Envelope.OPERATION);
        switch(operation) {
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
            if ("Alice".equals(did.getAlias())) {
                did.setStatus(DID.Status.ACTIVE);
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
        DID lid = (DID)envelope.getHeader(Envelope.DID);
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
        lid.setStatus(DID.Status.ACTIVE);
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
        did.setAuthenticated("1234".equals(did.getPassphrase()));
        did.setStatus(DID.Status.ACTIVE);
        reply(envelope);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("DIDService starting up....");
        System.out.println("DIDService started.");
        return true;
    }

}
