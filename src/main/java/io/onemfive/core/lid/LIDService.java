package io.onemfive.core.lid;

import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.MessageProducer;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.LID;

import java.util.Properties;

/**
 * Life IDentifier Service
 * @author objectorange
 */
public class LIDService extends BaseService {

    public LIDService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        String operation = (String) envelope.getHeader(Envelope.OPERATION);
        switch(operation) {
            case "Authenticate": authenticate(envelope);break;
            case "Create": create(envelope);break;
            default: deadLetter(envelope); // Operation not supported
        }
    }

    /**
     * Creates and returns LID
     * TODO: Option 1: connect to local Bote instance to create public/private keys
     * TODO: Option 2: role your own crypto for generating keys that can be used in I2P
     * @param envelope
     */
    private void create(Envelope envelope) {
        System.out.println(LIDService.class.getSimpleName()+": Received create request.");
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        LID lid = (LID)m.data.get(LID.class.getName());
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
        lid.setStatus(LID.Status.ACTIVE);
        envelope.setHeader(Envelope.REPLY,true);
        reply(envelope);
    }

    /**
     * Authenticates passphrase
     * TODO: Option 1: connect to local I2P Bote instance to validate passphrase
     * TODO: Option 2: role your own crypto for validating passphrases used in I2P Bote
     * @param envelope
     */
    private void authenticate(Envelope envelope) {
        System.out.println(LIDService.class.getSimpleName()+": Received authn request.");
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        LID lid = (LID)m.data.get(LID.class.getName());
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
        lid.setAuthenticated("1234".equals(lid.getPassphrase()));
        lid.setStatus(LID.Status.ACTIVE);
        envelope.setHeader(Envelope.REPLY,true);
        reply(envelope);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("LIDService starting up....");
        System.out.println("LIDService started.");
        return true;
    }

}
