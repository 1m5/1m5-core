package io.onemfive.core.keyring;

import io.onemfive.core.*;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;
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
    public static final String OPERATION_GET_PUBLIC_KEYS = "GET_PUBLIC_KEYS";
    public static final String OPERATION_ENCRYPT = "ENCRYPT";
    public static final String OPERATION_DECRYPT = "DECRYPT";
    public static final String OPERATION_SIGN = "SIGN";
    public static final String OPERATION_VERIFY_SIGNATURE = "VERIFY_SIGNATURE";

    public static final int PASSWORD_HASH_STRENGTH_64 = 0x10; // About 64 iterations for SHA-256
    public static final int PASSWORD_HASH_STRENGTH_128 = 0x20; // About 128
    public static final int PASSWORD_HASH_STRENGTH_256 = 0x30; // About 256
    public static final int PASSWORD_HASH_STRENGTH_130k = 0xc0; // About 130 thousand
    public static final int PASSWORD_HASH_STRENGTH_1M = 0xf0; // About 1 million
    public static final int PASSWORD_HASH_STRENGTH_2M = 0xff; // About 2 million

    private static final String PROVIDER_BOUNCY_CASTLE = "BC";

    private static final Logger LOG = Logger.getLogger(KeyRingService.class.getName());

    private Properties properties = new Properties();

    // Key Rings
    private String alias = "default";
    private char[] passphrase = "changeme".toCharArray();
    private int s2kCont = PASSWORD_HASH_STRENGTH_130k;

    private String skrPath = "skr";
    private File skr;
    private PGPSecretKeyRing secretKeyRing;

    private String pkrPath = "pkr";
    private File pkr;
    private PGPPublicKeyRing publicKeyRing;

    public KeyRingService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        switch (route.getOperation()) {
            case OPERATION_LOAD_KEY_RINGS: { loadKeyRings((LoadKeyRingsRequest)DLC.getData(LoadKeyRingsRequest.class,e));break; }
            case OPERATION_SAVE_KEY_RINGS: { saveKeyRings();break; }
            case OPERATION_GENERATE_KEY_PAIR: { generateKeyPair(e);break; }
            case OPERATION_STORE_PUBLIC_KEYS: { storePublicKeys(e);break; }
            case OPERATION_GET_PUBLIC_KEYS: { getPublicKeys(e);break; }
            case OPERATION_ENCRYPT: { encrypt(e);break; }
            case OPERATION_DECRYPT: { decrypt(e);break; }
            case OPERATION_SIGN: { sign(e);break; }
            case OPERATION_VERIFY_SIGNATURE: { verifySignature(e);break; }
            default: deadLetter(e);
        }
    }

    private void loadKeyRings(LoadKeyRingsRequest r) {
        if(r != null) {
            if(r.publicKeyRingCollectionFileLocation != null) {
                pkrPath = r.publicKeyRingCollectionFileLocation;
            }
            if(r.secretKeyRingCollectionFileLocation != null) {
                skrPath = r.secretKeyRingCollectionFileLocation;
            }
            if(r.hashStrength > 0) {
                s2kCont = r.hashStrength;
            }
            if(r.alias != null) {
                alias = r.alias;
            }
            if(r.passphrase != null) {
                passphrase = r.passphrase;
            }
        }

        skr = new File(skrPath);
        if(!skr.exists()) {
            try {
                if (!skr.createNewFile())
                    return;
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        pkr = new File(pkrPath);
        if(!pkr.exists()) {
            try {
                if (!pkr.createNewFile())
                    return;
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        // Try to load keys from current files
        try {
            FileInputStream fis = new FileInputStream(skr);
            secretKeyRing = new PGPSecretKeyRing(fis, new JcaKeyFingerprintCalculator());

            fis = new FileInputStream(pkr);
            publicKeyRing = new PGPPublicKeyRing(fis, new JcaKeyFingerprintCalculator());

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (PGPException ex) {
            ex.printStackTrace();
        }

        // If rings could not be loaded then generate them.
        if(secretKeyRing == null || publicKeyRing == null) {
            PGPKeyRingGenerator krgen = generateKeyRingGenerator(alias, passphrase, s2kCont);
            if (secretKeyRing == null) {
                // Create and save the Key Rings
                secretKeyRing = krgen.generateSecretKeyRing();
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(skr));
                    secretKeyRing.encode(bos);
                    bos.close();
                } catch (IOException e) {
                    LOG.warning(e.getLocalizedMessage());
                }
            }

            if (publicKeyRing == null) {
                // Create and save the Key Rings
                publicKeyRing = krgen.generatePublicKeyRing();
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pkr));
                    publicKeyRing.encode(bos);
                    bos.close();
                } catch (IOException e) {
                    LOG.warning(e.getLocalizedMessage());
                }
            }
        }
    }

    private void saveKeyRings() {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(skr));
            secretKeyRing.encode(bos);
            bos.close();
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
        }

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pkr));
            publicKeyRing.encode(bos);
            bos.close();
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
        }
    }

    private void generateKeyPair(Envelope e) {

    }

    private void storePublicKeys(Envelope e) {
        StorePublicKeysRequest r = (StorePublicKeysRequest)DLC.getData(StorePublicKeysRequest.class,e);

    }

    private void getPublicKeys(Envelope e) {
        GetPublicKeyRequest r = (GetPublicKeyRequest)DLC.getData(GetPublicKeyRequest.class,e);
        List<PGPPublicKey> publicKeys = new ArrayList<>();
        Iterator<PGPPublicKey> i = publicKeyRing.getPublicKeys();
        while(i.hasNext()) {
            publicKeys.add(i.next());
        }
        DLC.addData(PGPPublicKey.class, publicKeys, e);
    }

    private void encrypt(Envelope e) {
        DID didToEncrypt = e.getDID();
        byte[] contentToEncrypt = (byte[])DLC.getContent(e);

    }

    private void decrypt(Envelope e) {

    }

    private void sign(Envelope e) {
        DID didToSign = e.getDID();
        byte[] contentToSign = (byte[])DLC.getContent(e);
//        PGPPrivateKey privateKey = getPrivateKey(didToSign.getAlias(), didToSign.getPassphrase().toCharArray());
//        PGPSignatureGenerator sGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(pgpSecKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1).setProvider("BC"));
    }

    private void verifySignature(Envelope e) {

    }

    private PGPPublicKey getPublicKey(String alias) {
        PGPPublicKey key = null;
        if(publicKeyRing != null) {

        }
        return key;
    }

    private PGPPrivateKey getPrivateKey(String alias, char[] pass) throws PGPException {
        PGPSecretKey secretKey = getSecretKey(alias, pass);
        return secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));
    }

    private PGPSecretKey getSecretKey(String alias, char[] pass) {
        PGPSecretKey key = null;
        Iterator<PGPSecretKey> i = secretKeyRing.getSecretKeys();
        while(i.hasNext()) {
            PGPSecretKey secretKey = i.next();
//            if(secretKey.getPublicKey().)
        }
        return key;
    }

    private PGPKeyRingGenerator generateKeyRingGenerator(String alias, char[] passphrase, int s2kCount) {
        PGPKeyRingGenerator keyRingGen = null;
        try {
            RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
            /**
             * This value should be a Fermat number. 0x10001 (F4) is current recommended value. 3 (F1) is known to be safe also.
             * 3, 5, 17, 257, 65537, 4294967297, 18446744073709551617,
             * <p>
             * Practically speaking, Windows does not tolerate public exponents which do not fit in a 32-bit unsigned integer.
             * Using e=3 or e=65537 works "everywhere".
             * <p>
             * See: <a href="http://stackoverflow.com/questions/11279595/rsa-public-exponent-defaults-to-65537-what-should-this-value-be-what-are-the">stackoverflow: RSA Public exponent defaults to 65537. ... What are the impacts of my choices?</a>
             */
            BigInteger publicExponent = BigInteger.valueOf(0x10001);

            /**
             * As of 2018: 2048 is common value, 4096 is uncommon
             */
            int bitStrength = 4096;

            /**
             * How certain do we want to be that the chosen primes are really primes.
             * <p>
             * The higher this number, the more tests are done to make sure they are primes (and not composites).
             * <p>
             * See: <a href="http://crypto.stackexchange.com/questions/3114/what-is-the-correct-value-for-certainty-in-rsa-key-pair-generation">What is the correct value for “certainty” in RSA key pair generation?</a>
             * and
             * <a href="http://crypto.stackexchange.com/questions/3126/does-a-high-exponent-compensate-for-a-low-degree-of-certainty?lq=1">Does a high exponent compensate for a low degree of certainty?</a>
             *
             * As of 2018: 12 is common value, 16 uncommon, 80 rare
             */
            final int certainty = 80;

            kpg.init(new RSAKeyGenerationParameters(publicExponent, new SecureRandom(), bitStrength, certainty));

            // First create the master (signing) key with the generator.
            PGPKeyPair rsakp_sign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());

            // Then an encryption subkey.
            PGPKeyPair rsakp_enc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());

            // Add a self-signature on the id
            PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();
            // Add signed metadata on the signature.
            // 1) Declare its purpose
            signhashgen.setKeyFlags(false, KeyFlags.SIGN_DATA|KeyFlags.CERTIFY_OTHER);
            // 2) Set preferences for secondary crypto algorithms to use when sending messages to this key.
            signhashgen.setPreferredSymmetricAlgorithms
                    (false, new int[] {
                            SymmetricKeyAlgorithmTags.AES_256,
                            SymmetricKeyAlgorithmTags.AES_192,
                            SymmetricKeyAlgorithmTags.AES_128
                    });
            signhashgen.setPreferredHashAlgorithms
                    (false, new int[] {
                            HashAlgorithmTags.SHA256,
                            HashAlgorithmTags.SHA1,
                            HashAlgorithmTags.SHA384,
                            HashAlgorithmTags.SHA512,
                            HashAlgorithmTags.SHA224,
                    });
            // 3) Request senders add additional checksums to the
            //    message (useful when verifying unsigned messages.)
            signhashgen.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);

            // Create a signature on the encryption subkey.
            PGPSignatureSubpacketGenerator enchashgen = new PGPSignatureSubpacketGenerator();
            // Add metadata to declare its purpose
            enchashgen.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS|KeyFlags.ENCRYPT_STORAGE);
            // Objects used to encrypt the secret key.
            PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
            PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);
            PGPDigestCalculator sha512Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA512);

            // bcpg 1.48 exposes this API that includes s2kcount. Earlier
            // versions use a default of 0x60.
            PBESecretKeyEncryptor pske = (new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha256Calc, s2kCount)).build(passphrase);

            // Finally, create the keyring itself. The constructor
            // takes parameters that allow it to generate the self
            // signature.
            keyRingGen =
                    new PGPKeyRingGenerator
                            (PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign,
                                    alias, sha1Calc, signhashgen.generate(), null,
                                    new BcPGPContentSignerBuilder
                                            (rsakp_sign.getPublicKey().getAlgorithm(),
                                                    HashAlgorithmTags.SHA1),
                                    pske);

            // Add our encryption subkey, together with its signature.
            keyRingGen.addSubKey(rsakp_enc, enchashgen.generate(), null);
        } catch (PGPException ex) {
            LOG.severe(ex.getLocalizedMessage());
        }
        return keyRingGen;
    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        try {
            properties = Config.loadFromClasspath("keyring.config", p, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        Security.addProvider(new BouncyCastleProvider());

        String skrPath = (String)properties.get("1m5.keyring.secret.file");
        if(skrPath != null) {
            this.skrPath = skrPath;
        }
        String pkrPath = (String)properties.get("1m5.keyring.public.file");
        if(pkrPath != null) {
            this.pkrPath = pkrPath;
        }

        loadKeyRings(null);
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
        KeyRingService s = new KeyRingService(null, null);
        s.start(null);
    }

}
