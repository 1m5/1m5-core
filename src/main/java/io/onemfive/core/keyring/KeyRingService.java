package io.onemfive.core.keyring;

import io.onemfive.core.*;
import io.onemfive.core.util.SystemVersion;
import io.onemfive.data.Envelope;
import io.onemfive.data.PublicKey;
import io.onemfive.data.Route;
import io.onemfive.data.util.Base64;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.HashUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages keys for the bus and its user.
 *
 * @author ObjectOrange
 */
public class KeyRingService extends BaseService {

    public static final String OPERATION_GENERATE_KEY_RINGS_COLLECTIONS = "GENERATE_KEY_RINGS_COLLECTIONS";
    public static final String OPERATION_GENERATE_KEY_RINGS = "GENERATE_KEY_RINGS";
    public static final String OPERATION_AUTHN = "AUTHN";
    public static final String OPERATION_ENCRYPT = "ENCRYPT";
    public static final String OPERATION_DECRYPT = "DECRYPT";
    public static final String OPERATION_ENCRYPT_SYMMETRIC = "ENCRYPT_SYMMETRIC";
    public static final String OPERATION_DECRYPT_SYMMETRIC = "DECRYPT_SYMMETRIC";
    public static final String OPERATION_SIGN = "SIGN";
    public static final String OPERATION_VERIFY_SIGNATURE = "VERIFY_SIGNATURE";
    public static final String OPERATION_RELOAD = "RELOAD";

    public static final int PASSWORD_HASH_STRENGTH_64 = 0x10; // About 64 iterations for SHA-256
    public static final int PASSWORD_HASH_STRENGTH_128 = 0x20; // About 128
    public static final int PASSWORD_HASH_STRENGTH_256 = 0x30; // About 256
    public static final int PASSWORD_HASH_STRENGTH_512 = 0x40; // About 512
    public static final int PASSWORD_HASH_STRENGTH_1k = 0x50; // About 1k
    public static final int PASSWORD_HASH_STRENGTH_2k = 0x60; // About 2k
    public static final int PASSWORD_HASH_STRENGTH_4k = 0x70; // About 4k
    public static final int PASSWORD_HASH_STRENGTH_8k = 0x80; // About 8k
    public static final int PASSWORD_HASH_STRENGTH_16k = 0x90; // About16k
    public static final int PASSWORD_HASH_STRENGTH_32k = 0xa0; // About 32k
    public static final int PASSWORD_HASH_STRENGTH_64k = 0xb0; // About 64k
    public static final int PASSWORD_HASH_STRENGTH_128k = 0xc0; // About 128k
    public static final int PASSWORD_HASH_STRENGTH_256k = 0xd0; // About 256k
    public static final int PASSWORD_HASH_STRENGTH_512k = 0xe0; // About 512k
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
            case OPERATION_GENERATE_KEY_RINGS_COLLECTIONS: {
                LOG.info("Generate Key Rings Collections request received.");
                GenerateKeyRingCollectionsRequest r = (GenerateKeyRingCollectionsRequest)DLC.getData(GenerateKeyRingCollectionsRequest.class,e);
                if(r == null) {
                    LOG.warning("GenerateKeyRingCollectionsRequest required.");
                    r = new GenerateKeyRingCollectionsRequest();
                    r.errorCode = GenerateKeyRingCollectionsRequest.REQUEST_REQUIRED;
                    DLC.addData(GenerateKeyRingCollectionsRequest.class, r, e);
                    break;
                }
                if(r.keyRingUsername == null) {
                    LOG.warning("KeyRing username required.");
                    r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_USERNAME_REQUIRED;
                    break;
                }
                if(r.keyRingPassphrase == null) {
                    LOG.warning("KeyRing passphrase required.");
                    r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_PASSPHRASE_REQUIRED;
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
                        LOG.warning("KeyRing implementation unknown: "+r.keyRingImplementation);
                        r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                        return;
                    }
                    keyRing.generateKeyRingCollections(r);
                } catch (Exception ex) {
                    r.exception = ex;
                }
                break;
            }
            case OPERATION_AUTHN: {
                AuthNRequest r = (AuthNRequest)DLC.getData(AuthNRequest.class,e);
                if(r.keyRingUsername == null) {
                    LOG.warning("KeyRing username required.");
                    r.errorCode = AuthNRequest.KEY_RING_USERNAME_REQUIRED;
                    break;
                }
                if(r.keyRingPassphrase == null) {
                    LOG.warning("KeyRing passphrase required.");
                    r.errorCode = AuthNRequest.KEY_RING_PASSPHRASE_REQUIRED;
                    break;
                }
                if(r.alias == null || r.alias.isEmpty()) {
                    r.errorCode = AuthNRequest.ALIAS_REQUIRED;
                    break;
                }
                if(r.aliasPassphrase == null || r.aliasPassphrase.isEmpty()) {
                    r.errorCode = AuthNRequest.ALIAS_PASSPHRASE_REQUIRED;
                    break;
                }
                if(r.keyRingImplementation == null) {
                    r.keyRingImplementation = OpenPGPKeyRing.class.getName(); // Default
                }
                try {
                    keyRing = keyRings.get(r.keyRingImplementation);
                    if(keyRing == null) {
                        LOG.warning("KeyRing implementation unknown: "+r.keyRingImplementation);
                        r.errorCode = AuthNRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                        return;
                    }
                    PGPPublicKeyRingCollection c = null;
                    try {
                        c = keyRing.getPublicKeyRingCollection(r.keyRingUsername, r.keyRingPassphrase);
                    } catch (IOException e1) {
                        LOG.info("No key ring collection found.");
                    } catch (PGPException e1) {
                        LOG.info("No key ring collection found.");
                    }
                    if(c==null) {
                        // No collection found
                        if(r.autoGenerate) {
                            GenerateKeyRingCollectionsRequest gkr = new GenerateKeyRingCollectionsRequest();
                            gkr.keyRingUsername = r.keyRingUsername;
                            gkr.keyRingPassphrase = r.keyRingPassphrase;
                            keyRing.generateKeyRingCollections(gkr);
                            if(gkr.identityPublicKey!=null) r.identityPublicKey = gkr.identityPublicKey;
                            if(gkr.encryptionPublicKey!=null) r.encryptionPublicKey = gkr.encryptionPublicKey;
                        } else {
                            r.errorCode = AuthNRequest.ALIAS_UNKNOWN;
                            return;
                        }
                    } else {
                        PGPPublicKey identityPublicKey = keyRing.getPublicKey(c, r.alias, true);
                        r.identityPublicKey = new PublicKey();
                        r.identityPublicKey.setAlias(r.alias);
                        r.identityPublicKey.setFingerprint(Base64.encode(identityPublicKey.getFingerprint()));
                        r.identityPublicKey.setAddress(Base64.encode(identityPublicKey.getEncoded()));
                        r.identityPublicKey.isEncryptionKey(identityPublicKey.isEncryptionKey());
                        r.identityPublicKey.isIdentityKey(identityPublicKey.isMasterKey());
                        LOG.info("Identity Public Key loaded\n\tfingerprint: " + r.identityPublicKey.getFingerprint() + "\n\taddress: " + r.identityPublicKey.getAddress());

                        PGPPublicKey encryptionPublicKey = keyRing.getPublicKey(c, r.alias, false);
                        r.encryptionPublicKey = new PublicKey();
                        r.encryptionPublicKey.setAlias(r.alias);
                        r.encryptionPublicKey.setFingerprint(Base64.encode(encryptionPublicKey.getFingerprint()));
                        r.encryptionPublicKey.setAddress(Base64.encode(encryptionPublicKey.getEncoded()));
                        r.encryptionPublicKey.isEncryptionKey(encryptionPublicKey.isEncryptionKey());
                        r.encryptionPublicKey.isIdentityKey(encryptionPublicKey.isMasterKey());
                        LOG.info("Encryption Public Key loaded\n\tfingerprint: " + r.encryptionPublicKey.getFingerprint() + "\n\taddress: " + r.encryptionPublicKey.getAddress());
                    }
                } catch (Exception ex) {
                    r.exception = ex;
                    LOG.warning(ex.getLocalizedMessage());
                    ex.printStackTrace();
                }
                break;
            }
            case OPERATION_GENERATE_KEY_RINGS: {
                GenerateKeyRingsRequest r = (GenerateKeyRingsRequest)DLC.getData(GenerateKeyRingsRequest.class,e);
                if(r == null) {
                    r = new GenerateKeyRingsRequest();
                    r.errorCode = GenerateKeyRingsRequest.REQUEST_REQUIRED;
                    break;
                }
                if(r.keyRingUsername == null || r.keyRingUsername.isEmpty()) {
                    r.errorCode = GenerateKeyRingsRequest.KEYRING_USERNAME_REQUIRED;
                    break;
                }
                if(r.keyRingPassphrase == null || r.keyRingPassphrase.isEmpty()) {
                    r.errorCode = GenerateKeyRingsRequest.KEYRING_PASSPHRASE_REQUIRED;
                    break;
                }
                if(r.alias == null || r.alias.isEmpty()) {
                    r.errorCode = GenerateKeyRingsRequest.ALIAS_REQUIRED;
                    break;
                }
                if(r.aliasPassphrase == null || r.aliasPassphrase.isEmpty()) {
                    r.errorCode = GenerateKeyRingsRequest.ALIAS_PASSPHRASE_REQUIRED;
                    break;
                }
                if(r.keyRingImplementation == null)
                    r.keyRingImplementation = OpenPGPKeyRing.class.getName(); // default
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.createKeyRings(r.keyRingUsername, r.keyRingPassphrase, r.alias, r.aliasPassphrase, r.hashStrength);
                } catch (Exception ex) {
                    r.exception = ex;
                    LOG.warning(ex.getLocalizedMessage());
                    ex.printStackTrace();
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
                if(r.publicKeyAlias == null) {
                    r.errorCode = EncryptRequest.PUBLIC_KEY_ALIAS_REQUIRED;
                    break;
                }
                if(r.contentToEncrypt == null || r.contentToEncrypt.length == 0) {
                    r.errorCode = EncryptRequest.CONTENT_TO_ENCRYPT_REQUIRED;
                    break;
                }
                keyRing = keyRings.get(r.keyRingImplementation);
                if(keyRing == null) {
                    r.errorCode = EncryptRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.encrypt(r);
                } catch (Exception ex) {
                    r.exception = ex;
                    LOG.warning(ex.getLocalizedMessage());
                    ex.printStackTrace();
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
                    r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.decrypt(r);
                } catch (Exception ex) {
                    r.exception = ex;
                    LOG.warning(ex.getLocalizedMessage());
                    ex.printStackTrace();
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
                    r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.sign(r);
                } catch (Exception ex) {
                    r.exception = ex;
                    LOG.warning(ex.getLocalizedMessage());
                    ex.printStackTrace();
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
                    r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_IMPLEMENTATION_UNKNOWN;
                    return;
                }
                try {
                    keyRing.verifySignature(r);
                } catch (Exception ex) {
                    r.exception = ex;
                    LOG.warning(ex.getLocalizedMessage());
                    ex.printStackTrace();
                }
                break;
            }
            case OPERATION_ENCRYPT_SYMMETRIC: {
                EncryptSymmetricRequest r = (EncryptSymmetricRequest)DLC.getData(EncryptSymmetricRequest.class,e);
                if(r==null) {
                    r = new EncryptSymmetricRequest();
                    r.errorCode = EncryptSymmetricRequest.REQUEST_REQUIRED;
                    DLC.addData(EncryptSymmetricRequest.class, r, e);
                    break;
                }
                if(r.passphrase == null || r.passphrase.isEmpty()) {
                    r.errorCode = EncryptSymmetricRequest.PASSPHRASE_REQUIRED;
                    break;
                }
                if(r.contentToEncrypt == null || r.contentToEncrypt.length == 0) {
                    r.errorCode = EncryptSymmetricRequest.CONTENT_TO_ENCRYPT_REQUIRED;
                    break;
                }
                try {
                    byte[] key = r.passphrase.getBytes("UTF-8");
                    MessageDigest sha = MessageDigest.getInstance("SHA-1");
                    key = sha.digest(key);
                    key = Arrays.copyOf(key,16);
                    // Encrypt
                    SecretKey secretKey = new SecretKeySpec(key, "AES");
                    Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    byte[] iv = new byte[16];
                    SecureRandom random = new SecureRandom();
                    random.nextBytes(iv);
                    r.iv = java.util.Base64.getEncoder().encodeToString(iv);
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                    aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
                    r.encryptedContent = aesCipher.doFinal(r.contentToEncrypt);
                } catch (UnsupportedEncodingException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (NoSuchAlgorithmException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (NoSuchPaddingException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (InvalidKeyException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (InvalidAlgorithmParameterException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (IllegalBlockSizeException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (BadPaddingException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                }

                break;
            }
            case OPERATION_DECRYPT_SYMMETRIC: {
                DecryptSymmetricRequest r = (DecryptSymmetricRequest)DLC.getData(DecryptSymmetricRequest.class,e);
                if(r.encryptedContent==null || r.encryptedContent.length == 0) {
                    r.errorCode = DecryptSymmetricRequest.ENCRYPTED_CONTENT_REQUIRED;
                    break;
                }
                if(r.passphrase==null || r.passphrase.isEmpty()) {
                    r.errorCode = DecryptSymmetricRequest.PASSPHRASE_REQUIRED;
                    break;
                }
                if(r.iv==null || r.iv.isEmpty()) {
                    r.errorCode = DecryptSymmetricRequest.IV_REQUIRED;
                    break;
                }
                try {
                    byte[] key = r.passphrase.getBytes("UTF-8");
                    MessageDigest sha = MessageDigest.getInstance("SHA-1");
                    key = sha.digest(key);
                    key = Arrays.copyOf(key,16);
                    // Encrypt
                    SecretKey secretKey = new SecretKeySpec(key, "AES");
                    Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    byte[] iv = java.util.Base64.getDecoder().decode(r.iv);
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                    aesCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
                    r.decryptedContent = aesCipher.doFinal(r.encryptedContent);
                } catch (UnsupportedEncodingException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (NoSuchAlgorithmException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (NoSuchPaddingException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (InvalidKeyException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (InvalidAlgorithmParameterException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (IllegalBlockSizeException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                } catch (BadPaddingException e1) {
                    r.errorCode = DecryptSymmetricRequest.BAD_PASSPHRASE;
                    LOG.warning(e1.getLocalizedMessage());
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

}
