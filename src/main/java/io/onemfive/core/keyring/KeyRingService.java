package io.onemfive.core.keyring;

import io.onemfive.core.*;
import io.onemfive.core.util.SystemVersion;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages keys for the bus and its user.
 *
 * @author ObjectOrange
 */
public class KeyRingService extends BaseService {

    public static final String OPERATION_LOAD_KEY_RINGS = "LOAD_KEY_RINGS";
    public static final String OPERATION_SAVE_KEY_RINGS = "SAVE_KEY_RINGS";
    public static final String OPERATION_GENERATE_KEY_PAIR = "GENERATE_KEY_PAIR";
    public static final String OPERATION_STORE_PUBLIC_KEYS = "STORE_PUBLIC_KEYS";
    public static final String OPERATION_GET_PUBLIC_KEY = "GET_PUBLIC_KEY";
    public static final String OPERATION_ENCRYPT = "ENCRYPT";
    public static final String OPERATION_DECRYPT = "DECRYPT";
    public static final String OPERATION_SIGN = "SIGN";
    public static final String OPERATION_VERIFY_SIGNATURE = "VERIFY_SIGNATURE";
    public static final String OPERATION_RELOAD = "RELOAD";

    public static final int PASSWORD_HASH_STRENGTH_64 = 0x10; // About 64 iterations for SHA-256
    public static final int PASSWORD_HASH_STRENGTH_128 = 0x20; // About 128
    public static final int PASSWORD_HASH_STRENGTH_256 = 0x30; // About 256
    public static final int PASSWORD_HASH_STRENGTH_130k = 0xc0; // About 130 thousand
    public static final int PASSWORD_HASH_STRENGTH_1M = 0xf0; // About 1 million
    public static final int PASSWORD_HASH_STRENGTH_2M = 0xff; // About 2 million

    private static final Logger LOG = Logger.getLogger(KeyRingService.class.getName());

    private Properties properties = new Properties();

    private Map<String,KeyRing> keyRings = new HashMap<>();

    public KeyRingService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        KeyRing keyRing;
        switch (route.getOperation()) {
            case OPERATION_LOAD_KEY_RINGS: {
                LoadKeyRingsRequest r = (LoadKeyRingsRequest)DLC.getData(LoadKeyRingsRequest.class,e);
                if(r == null) {
                    r = new LoadKeyRingsRequest();
                    r.errorCode = LoadKeyRingsRequest.REQUEST_REQUIRED;
                    DLC.addData(LoadKeyRingsRequest.class, r, e);
                    break;
                }
                if(r.keyRingAlias == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_ALIAS_REQUIRED;
                    break;
                }
                if(r.keyRingPassphrase == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_PASSPHRASE_REQUIRED;
                    break;
                }
                if(r.secretKeyRingCollectionFileLocation == null) {
                    r.errorCode = LoadKeyRingsRequest.SKR_LOCATION_NOT_PROVIDED;
                    break;
                }
                if(r.publicKeyRingCollectionFileLocation == null) {
                    r.errorCode = LoadKeyRingsRequest.PKR_LOCATION_NOT_PROVIDED;
                    break;
                }
                if(!r.autoGenerate && r.removeOldKeys) {
                    r.errorCode = LoadKeyRingsRequest.AUTOGENERATE_REMOVE_OLD_KEYS_CONFLICT;
                    r.errorMessage = "If removeOldKeys is set to true, autoGenerate must also be set to true to generate the keys.";
                    break;
                }
                if(r.hashStrength < PASSWORD_HASH_STRENGTH_64) {
                    r.hashStrength = PASSWORD_HASH_STRENGTH_64; // minimum
                }
                if(r.keyRingImplementation == null) {
                    r.keyRingImplementation = OpenPGPKeyRing.class.getName(); // Default
                }
                try {
                    keyRing = keyRings.get(r.keyRingImplementation);
                    if(keyRing == null) {
                        r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                        return;
                    }
                    keyRing.loadKeyRings(r);
                } catch (Exception ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_SAVE_KEY_RINGS: {
                SaveKeyRingsRequest r = (SaveKeyRingsRequest)DLC.getData(SaveKeyRingsRequest.class,e);
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                keyRing.saveKeyRings();break;
            }
            case OPERATION_GENERATE_KEY_PAIR: {
                GenerateKeyPairRequest r = (GenerateKeyPairRequest)DLC.getData(GenerateKeyPairRequest.class,e);
                if(r == null) {
                    r = new GenerateKeyPairRequest();
                    r.errorCode = GenerateKeyPairRequest.REQUEST_REQUIRED;
                    break;
                }
                if(r.alias == null || r.alias.isEmpty()) {
                    r.errorCode = GenerateKeyPairRequest.ALIAS_REQUIRED;
                    break;
                }
                if(r.passphrase == null || r.passphrase.length == 0) {
                    r.errorCode = GenerateKeyPairRequest.PASSPHRASE_REQUIRED;
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.generateKeyRings(r.alias, r.passphrase, r.hashStrength);
                } catch (Exception ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_STORE_PUBLIC_KEYS: {
                StorePublicKeysRequest r = (StorePublicKeysRequest)DLC.getData(StorePublicKeysRequest.class,e);
                if(r == null) {
                    r = new StorePublicKeysRequest();
                    r.errorCode = StorePublicKeysRequest.REQUEST_REQUIRED;
                    break;
                }
                if(r.keyId == 0) {
                    r.errorCode = StorePublicKeysRequest.KEYID_REQUIRED;
                    break;
                }
                if(r.publicKeys == null || r.publicKeys.size() == 0) {
                    r.errorCode = StorePublicKeysRequest.PUBLIC_KEYS_LIST_REQUIRED;
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                if(keyRing.getPublicKeyRingCollection() == null) {
                    r.errorCode = StorePublicKeysRequest.NON_EXISTANT_PUBLIC_KEY_RING_COLLECTION;
                    break;
                }
                try {
                    keyRing.storePublicKeys(r);
                } catch (PGPException ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_GET_PUBLIC_KEY: {
                GetPublicKeyRequest r = (GetPublicKeyRequest)DLC.getData(GetPublicKeyRequest.class,e);
                if(r == null) {
                    r = new GetPublicKeyRequest();
                    r.errorCode = GetPublicKeyRequest.REQUEST_REQUIRED;
                    break;
                }
                if((r.alias == null || r.alias.isEmpty()) && (r.fingerprint == null || r.fingerprint.length == 0)) {
                    r.errorCode = GetPublicKeyRequest.ALIAS_OR_FINGERPRINT_REQUIRED;
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    if(r.alias != null)
                        r.publicKey = keyRing.getPublicKey(r.alias, r.master);
                    else
                        r.publicKey = keyRing.getPublicKey(r.fingerprint);
                } catch (PGPException ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_ENCRYPT: {
                EncryptRequest r = (EncryptRequest)DLC.getData(EncryptRequest.class, e);
                if(r == null) {
                    r = new EncryptRequest();
                    r.errorCode = EncryptRequest.REQUEST_REQUIRED;
                    DLC.addData(EncryptRequest.class, r, e);
                    break;
                }
                if(r.fingerprint == null || r.fingerprint.length == 0) {
                    r.errorCode = EncryptRequest.FINGERPRINT_REQUIRED;
                    break;
                }
                if(r.contentToEncrypt == null || r.contentToEncrypt.length == 0) {
                    r.errorCode = EncryptRequest.CONTENT_TO_ENCRYPT_REQUIRED;
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.encrypt(r);
                } catch (Exception ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_DECRYPT: {
                DecryptRequest r = (DecryptRequest)DLC.getData(DecryptRequest.class, e);
                if(r == null) {
                    r = new DecryptRequest();
                    r.errorCode = DecryptRequest.REQUEST_REQUIRED;
                    DLC.addData(DecryptRequest.class, r, e);
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.decrypt(r);
                } catch (Exception ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_SIGN: {
                SignRequest r = (SignRequest)DLC.getData(SignRequest.class, e);
                if(r == null) {
                    r = new SignRequest();
                    r.errorCode = SignRequest.REQUEST_REQUIRED;
                    DLC.addData(SignRequest.class, r, e);
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.sign(r);
                } catch (Exception ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_VERIFY_SIGNATURE: {
                VerifySignatureRequest r = (VerifySignatureRequest)DLC.getData(VerifySignatureRequest.class,e);
                if(r == null) {
                    r = new VerifySignatureRequest();
                    r.errorCode = VerifySignatureRequest.REQUEST_REQUIRED;
                    DLC.addData(VerifySignatureRequest.class, r, e);
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = LoadKeyRingsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.verifySignature(r);
                } catch (Exception ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_RELOAD: {
                loadKeyRingImplementations();
            }
            default: deadLetter(e);
        }
    }

    private void loadKeyRingImplementations(){
        keyRings.clear();
        if(properties.getProperty(KeyRing.class.getName()) != null) {
            String[] keyRingStrings = properties.getProperty(KeyRing.class.getName()).split(",");
            KeyRing keyRing;
            for(String keyRingString : keyRingStrings) {
                try {
                    keyRing = (KeyRing) Class.forName(keyRingString).newInstance();
                    keyRing.init(properties);
                    keyRings.put(keyRingString, keyRing);
                } catch (Exception e) {
                    LOG.warning(e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public boolean start(Properties p) {
//        super.start(p);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        try {
            properties = Config.loadFromClasspath("keyring.config", p, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        // Android apps set SpongyCastle as the default provider
        if(!SystemVersion.isAndroid()) {
            Security.addProvider(new BouncyCastleProvider());
        }

        loadKeyRingImplementations();

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        super.gracefulShutdown();
        LOG.info("Gracefully shutting down...");
        updateStatus(ServiceStatus.GRACEFULLY_SHUTTING_DOWN);

        updateStatus(ServiceStatus.GRACEFULLY_SHUTDOWN);
        LOG.info("Gracefully Shutdown");
        return true;
    }

    public static void main(String[] args) {
        boolean isArmored = false;
        char[] passphrase = "1234".toCharArray();
        boolean integrityCheck = true;
        int s3kCount = 12;

        // Alice
        KeyRingService sAlice = new KeyRingService(null, null);
        sAlice.start(null);
        String aliasAlice = "Alice";

        // Charlie
//        KeyRingService sCharlie = new KeyRingService(null, null);
//        sCharlie.start(null);
//        String aliasCharlie = "Charlie";

        // Load Key Rings
        LoadKeyRingsRequest lr = new LoadKeyRingsRequest();
        lr.keyRingAlias = aliasAlice;
        lr.keyRingPassphrase = passphrase;
        lr.secretKeyRingCollectionFileLocation = "alice.skr";
        lr.publicKeyRingCollectionFileLocation = "alice.pkr";
        Envelope e1 = Envelope.documentFactory();
        DLC.addData(LoadKeyRingsRequest.class, lr, e1);
        DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_LOAD_KEY_RINGS, e1);
        e1.setRoute(e1.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router

//        LoadKeyRingsRequest charlieRequest = new LoadKeyRingsRequest();
//        charlieRequest.keyRingAlias = aliasCharlie;
//        charlieRequest.keyRingPassphrase = passphrase;
//        charlieRequest.secretKeyRingCollectionFileLocation = "charlie.skr";
//        charlieRequest.publicKeyRingCollectionFileLocation = "charlie.pkr";
//        Envelope e2 = Envelope.documentFactory();
//        DLC.addData(LoadKeyRingsRequest.class, charlieRequest, e2);
//        DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_LOAD_KEY_RINGS, e2);

        long start = new Date().getTime();
        sAlice.handleDocument(e1);
//        sCharlie.handleDocument(e2);
        long end = new Date().getTime();
        long duration = end - start;

        System.out.println("Load KeyRings Duration: "+duration);

        // Generate New Alias Key Ring
//        init = new Date().getTime();
//        try {
//            sAlice.generateKeyRings("Barbara",passphrase, PASSWORD_HASH_STRENGTH_64);
//            sCharlie.generateKeyRings("Dan",passphrase, PASSWORD_HASH_STRENGTH_64);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (PGPException e) {
//            e.printStackTrace();
//        }
//        end = new Date().getTime();
//        duration = end - init;
//        System.out.println("Generate New Alias Key Ring Duration: "+duration);

        // Verify we have master and encryption public keys
//        start = new Date().getTime();
//        try {
//            // Alice
//            PGPPublicKey kAliceM = sAlice.keyRing.getPublicKey("Alice", true);
//            assert(kAliceM != null && kAliceM.isMasterKey());
//
//            PGPPublicKey kAliceE = sAlice.keyRing.getPublicKey("Alice", false);
//            assert(kAliceE != null && kAliceE.isEncryptionKey());
//
//            // Charlie
//            PGPPublicKey kCharlieM = sCharlie.keyRing.getPublicKey("Charlie", true);
//            assert(kCharlieM != null && kCharlieM.isMasterKey());
//
//            PGPPublicKey kCharlieE = sCharlie.keyRing.getPublicKey("Charlie", false);
//            assert(kCharlieE != null && kCharlieE.isEncryptionKey());
//
//            // Alice - Barbara
//            PGPPublicKey kBarbaraM = sAlice.keyRing.getPublicKey("Barbara", true);
//            assert(kBarbaraM != null && kBarbaraM.isMasterKey());
//
//            PGPPublicKey kBarbaraE = sAlice.keyRing.getPublicKey("Barbara", false);
//            assert(kBarbaraE != null && kBarbaraE.isEncryptionKey());
//
//            // Charlie - Dan
//            PGPPublicKey kDanM = sCharlie.keyRing.getPublicKey("Dan", true);
//            assert(kDanM != null && kDanM.isMasterKey());
//
//            PGPPublicKey kDanE = sCharlie.keyRing.getPublicKey("Dan", false);
//            assert(kDanE != null && kDanE.isEncryptionKey());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        end = new Date().getTime();
//        duration = end - start;
//        System.out.println("Get Public Key Duration: "+duration);

        // Add each other's public keys
//        start = new Date().getTime();
//        try {
//            StorePublicKeysRequest r = new StorePublicKeysRequest();
//
//            PGPPublicKey cK = sCharlie.keyRing.getPublicKey("Charlie", false);
//            PGPPublicKey aK = sAlice.keyRing.getPublicKey("Alice", false);
//
//            List<PGPPublicKey> cPublicKeys = new ArrayList<>();
//            cPublicKeys.add(cK);
//            r.keyId = cK.getKeyID();
//            r.publicKeys = cPublicKeys;
//            sAlice.keyRing.storePublicKeys(r);
//
//            List<PGPPublicKey> aPublicKeys = new ArrayList<>();
//            aPublicKeys.add(aK);
//            r.keyId = aK.getKeyID();
//            r.publicKeys = aPublicKeys;
//            sCharlie.keyRing.storePublicKeys(r);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        end = new Date().getTime();
//        duration = end - start;
//        System.out.println("Add Public Key Duration: "+duration);

        // Encrypt
        try {
            if(lr.publicKey.getFingerprint() != null) {
                EncryptRequest er = new EncryptRequest();
                er.fingerprint = lr.publicKey.getFingerprint().getBytes();
                String contentStringToEncrypt = "Hello Charlie!";
                er.contentToEncrypt = contentStringToEncrypt.getBytes();
                Envelope ee = Envelope.documentFactory();
                DLC.addData(EncryptRequest.class, er, ee);
                DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_ENCRYPT, ee);
                ee.setRoute(ee.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
                start = new Date().getTime();
                sAlice.handleDocument(ee);
                end = new Date().getTime();
                duration = end - start;
                LOG.info("Encrypt Duration: "+duration);
                LOG.info("Encrypted content: "+new String(er.encryptedContent));
                assert (er.errorCode == EncryptRequest.NO_ERROR && er.encryptedContent != null && er.encryptedContent.length > 0);

                DecryptRequest dr = new DecryptRequest();
                dr.encryptedContent = er.encryptedContent;
                dr.alias = aliasAlice;
                dr.passphrase = passphrase;
                Envelope de = Envelope.documentFactory();
                DLC.addData(DecryptRequest.class, dr, de);
                DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_DECRYPT, de);
                de.setRoute(de.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
                start = new Date().getTime();
                sAlice.handleDocument(de);
                duration = end - start;
                LOG.info("Decrypt Duration: "+duration);
                String contentStringDecrypted = new String(dr.plaintextContent);
                LOG.info("Decrypted content: "+contentStringDecrypted);
                assert (contentStringToEncrypt.equals(contentStringDecrypted));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
