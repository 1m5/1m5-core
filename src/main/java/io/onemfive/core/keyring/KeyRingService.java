package io.onemfive.core.keyring;

import io.onemfive.core.*;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;
import org.bouncycastle.bcpg.*;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.*;
import org.bouncycastle.openpgp.operator.jcajce.*;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
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

    private File skr;
    // a secret key with master key and any sub-keys
    private PGPSecretKeyRing secretKeyRing;
    private File pkr;
    // a public key with master key and any sub-keys
    private PGPPublicKeyRing publicKeyRing;

    public KeyRingService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        switch (route.getOperation()) {
            case OPERATION_LOAD_KEY_RINGS: {
                LoadKeyRingsRequest r = (LoadKeyRingsRequest)DLC.getData(LoadKeyRingsRequest.class,e);
                if(r == null) {
                    LOG.warning(LoadKeyRingsRequest.class.getName() + " required as parameter.");
                    return;
                }
                if(r.alias == null) {
                    LOG.warning("Alias is required.");
                    return;
                }
                if(r.passphrase == null) {
                    LOG.warning("Passphrase is required.");
                    return;
                }
                if(r.hashStrength < PASSWORD_HASH_STRENGTH_64) {
                    r.hashStrength = PASSWORD_HASH_STRENGTH_130k;
                }
                if(r.secretKeyRingCollectionFileLocation == null) {
                    r.secretKeyRingCollectionFileLocation = "skr";
                }
                if(r.publicKeyRingCollectionFileLocation == null) {
                    r.publicKeyRingCollectionFileLocation = "pkr";
                }
                loadKeyRings(r.alias, r.passphrase, r.hashStrength, r.secretKeyRingCollectionFileLocation, r.publicKeyRingCollectionFileLocation, r.autoGenerate);
                break;
            }
            case OPERATION_SAVE_KEY_RINGS: { saveKeyRings();break; }
            case OPERATION_GENERATE_KEY_PAIR: {
                GenerateKeyPairRequest r = (GenerateKeyPairRequest)DLC.getData(GenerateKeyPairRequest.class,e);
                if(r == null) {
                    LOG.warning(GenerateKeyPairRequest.class.getName()+" required in Envelope data.");
                    return;
                }
                if(r.alias == null) {
                    LOG.warning("Alias required.");
                    return;
                }
                if(r.passphrase == null) {
                    LOG.warning("Passphrase required.");
                    return;
                }
                generateKeyPair(r.alias, r.passphrase);
                break;
            }
            case OPERATION_STORE_PUBLIC_KEYS: {
                StorePublicKeysRequest r = (StorePublicKeysRequest)DLC.getData(StorePublicKeysRequest.class,e);
                if(r == null) {
                    LOG.warning(StorePublicKeysRequest.class.getName()+" required.");
                    return;
                }
                if(r.publicKeys == null || r.publicKeys.size() == 0) {
                    LOG.warning("A list of PGPPublicKeys greater than 0 is required to store.");
                    return;
                }
                if(publicKeyRing == null) {
                    LOG.warning(PGPPublicKeyRing.class.getName()+" must be created first.");
                    return;
                }
                storePublicKeys(r.publicKeys);
                break;
            }
            case OPERATION_GET_PUBLIC_KEYS: {
                GetPublicKeyRequest r = (GetPublicKeyRequest)DLC.getData(GetPublicKeyRequest.class,e);
                r.publicKeys = getPublicKeys(r.alias);
                break;
            }
            case OPERATION_ENCRYPT: {
                EncryptRequest r = (EncryptRequest)DLC.getData(EncryptRequest.class, e);
                try {
                    r.encryptedContent = encrypt(r.plainTextContent, r.alias, r.passphrase);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (PGPException e1) {
                    e1.printStackTrace();
                }
                break;
            }
            case OPERATION_DECRYPT: {
                DecryptRequest r = (DecryptRequest)DLC.getData(DecryptRequest.class, e);
                try {
                    r.plaintextContent = decrypt(r.encryptedContent, r.alias, r.passphrase);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (PGPException e1) {
                    e1.printStackTrace();
                }
                break;
            }
            case OPERATION_SIGN: {
                SignRequest r = (SignRequest)DLC.getData(SignRequest.class, e);
                try {
                    r.signature = sign(r.contentToSign, r.alias, r.passphrase);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (PGPException e1) {
                    e1.printStackTrace();
                }
                break;
            }
            case OPERATION_VERIFY_SIGNATURE: {
                VerifySignatureRequest r = (VerifySignatureRequest)DLC.getData(VerifySignatureRequest.class,e);
                try {
                    r.verified = verifySignature(r.contentSigned, r.signature, r.alias);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (PGPException e1) {
                    e1.printStackTrace();
                }
                break;
            }
            default: deadLetter(e);
        }
    }

    private void loadKeyRings(String alias,
                              char[] passphrase,
                              int hashStrength,
                              String secretKeyRingCollectionFileLocation,
                              String publicKeyRingCollectionFileLocation,
                              boolean autoGenerate) {

        boolean newFiles = false;
        skr = new File(secretKeyRingCollectionFileLocation);
        if(!skr.exists()) {
            newFiles = true;
            try {
                if (!skr.createNewFile())
                    return;
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        pkr = new File(publicKeyRingCollectionFileLocation);
        if(!pkr.exists()) {
            newFiles = true;
            try {
                if (!pkr.createNewFile())
                    return;
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        if(!newFiles) {
            // Try to load keys from files
            try {
                // TODO: Decrypt files
                FileInputStream fis = new FileInputStream(skr);
                secretKeyRing = new PGPSecretKeyRing(fis, new BcKeyFingerprintCalculator());

                fis = new FileInputStream(pkr);
                publicKeyRing = new PGPPublicKeyRing(fis, new BcKeyFingerprintCalculator());

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (PGPException ex) {
                ex.printStackTrace();
            }
        }

        // If rings could not be loaded then generate them if an alias and passphrase are provided.
        if(secretKeyRing == null || publicKeyRing == null && autoGenerate) {
            // TODO: Encrypt files
            PGPKeyRingGenerator krgen = generateKeyRingGenerator(alias, passphrase, hashStrength);
            // Create and save the Key Rings
            secretKeyRing = krgen.generateSecretKeyRing();
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(skr));
                secretKeyRing.encode(bos);
                bos.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }

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

    private void saveKeyRings() {
        // TODO: Encrypt files
        if(secretKeyRing != null && skr != null) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(skr));
                secretKeyRing.encode(bos);
                bos.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }

        if(publicKeyRing != null && pkr != null) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pkr));
                publicKeyRing.encode(bos);
                bos.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
    }

    private void generateKeyPair(String alias, char[] passphrase) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", PROVIDER_BOUNCY_CASTLE);
            kpg.initialize(1024);
            KeyPair kp = kpg.generateKeyPair();
            PublicKey publicKey = kp.getPublic();
            PrivateKey privateKey = kp.getPrivate();

            PGPPublicKey a = (new JcaPGPKeyConverter().getPGPPublicKey(PGPPublicKey.RSA_GENERAL, publicKey, new Date()));
            RSAPrivateCrtKey rsK = (RSAPrivateCrtKey)privateKey;
            RSASecretBCPGKey privPk = new RSASecretBCPGKey(rsK.getPrivateExponent(), rsK.getPrimeP(), rsK.getPrimeQ());
            PGPPrivateKey b = new PGPPrivateKey(a.getKeyID(), a.getPublicKeyPacket(), privPk);

            PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
            PGPKeyPair keyPair = new PGPKeyPair(a,b);

            PGPSecretKey secretKey = new PGPSecretKey(
                    PGPSignature.DEFAULT_CERTIFICATION, keyPair, alias, sha1Calc, null, null,
                    new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
                    new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.CAST5, sha1Calc).setProvider(PROVIDER_BOUNCY_CASTLE).build(passphrase));

            PGPSecretKeyRing.insertSecretKey(secretKeyRing, secretKey);

            PGPPublicKey key = secretKey.getPublicKey();

            PGPPublicKeyRing.insertPublicKey(publicKeyRing, key);

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (NoSuchProviderException e1) {
            e1.printStackTrace();
        } catch (PGPException e1) {
            e1.printStackTrace();
        }

    }

    private void storePublicKeys(List<PGPPublicKey> publicKeys) {
        for (PGPPublicKey k : publicKeys) {
            PGPPublicKeyRing.insertPublicKey(publicKeyRing, k);
        }
        // Ensure they're persisted
        saveKeyRings();
    }

    private PGPPublicKey getFirstPublicKey(String alias) {
        List<PGPPublicKey> publicKeys = getPublicKeys(alias);
        if(publicKeys != null && publicKeys.size() > 0)
            return publicKeys.get(0);
        else
            return null;
    }

    private List<PGPPublicKey> getPublicKeys(String alias) {
        List<PGPPublicKey> publicKeys = new ArrayList<>();
        Iterator<PGPPublicKey> i = publicKeyRing.getPublicKeys();
        if(alias != null) {
            while(i.hasNext()) {
                PGPPublicKey k = i.next();
                Iterator<String> aliases = k.getUserIDs();
                while(aliases.hasNext()) {
                    if(alias.equals(aliases.next())) {
                        publicKeys.add(k);
                        break;
                    }
                }
            }
        } else {
            while(i.hasNext()) {
                publicKeys.add(i.next());
            }
        }
        return publicKeys;
    }

    private byte[] encrypt(byte[] plainTextContent, String alias, char[] passphrase) throws IOException, PGPException {

        PGPPublicKey publicKey = getFirstPublicKey(alias);
        if(publicKey == null) {
            LOG.warning("Unable to find publicKey for alias.");
            return null;
        }

        boolean withIntegrityCheck = true;

        ByteArrayOutputStream content = new ByteArrayOutputStream();

        OutputStream out = new ArmoredOutputStream(content);

        // Compress content
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(comData.open(bOut), PGPLiteralData.BINARY, "sec", plainTextContent.length, new Date());
        pOut.write(plainTextContent);

        lData.close();
        comData.close();

        // Encrypt content
        JcePGPDataEncryptorBuilder c = new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                .setWithIntegrityPacket(withIntegrityCheck)
                .setSecureRandom(new SecureRandom())
                .setProvider(PROVIDER_BOUNCY_CASTLE);

        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(c);

        JcePublicKeyKeyEncryptionMethodGenerator d = new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
                .setProvider(PROVIDER_BOUNCY_CASTLE)
                .setSecureRandom(new SecureRandom());

        cPk.addMethod(d);

        byte[] bytes = bOut.toByteArray();

        OutputStream cOut = cPk.open(out, bytes.length);

        cOut.write(bytes);

        cOut.close();

        out.close();

        return content.toByteArray();
    }

    private byte[] decrypt(byte[] encryptedContent, String alias, char[] passphrase) throws IOException, PGPException {

        InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedContent));
        PGPObjectFactory pgpF = new PGPObjectFactory(in, new BcKeyFingerprintCalculator());
        PGPEncryptedDataList enc;
        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof  PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }
        //
        // find the secret key
        //
        Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
        PGPPrivateKey privKey = null;
        PGPPublicKeyEncryptedData pbe = null;
        PGPSecretKey secKey = null;
        while (privKey == null && it.hasNext()) {
            pbe = it.next();
            secKey = getSecretKey(pbe.getKeyID());
            if(secKey != null) {
                PBESecretKeyDecryptor a = new JcePBESecretKeyDecryptorBuilder(
                        new JcaPGPDigestCalculatorProviderBuilder()
                                .setProvider(PROVIDER_BOUNCY_CASTLE).build())
                        .setProvider(PROVIDER_BOUNCY_CASTLE).build(passphrase);
                privKey = secKey.extractPrivateKey(a);
            }
        }

        PublicKeyDataDecryptorFactory b = new JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(PROVIDER_BOUNCY_CASTLE).setContentProvider(PROVIDER_BOUNCY_CASTLE).build(privKey);

        InputStream clear = pbe.getDataStream(b);

        PGPObjectFactory plainFact = new PGPObjectFactory(clear,new BcKeyFingerprintCalculator());

        Object message = plainFact.nextObject();

        if (message instanceof  PGPCompressedData) {
            PGPCompressedData cData = (PGPCompressedData) message;
            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream(), new BcKeyFingerprintCalculator());
            message = pgpFact.nextObject();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (message instanceof  PGPLiteralData) {
            PGPLiteralData ld = (PGPLiteralData) message;
            InputStream unc = ld.getInputStream();
            int ch;
            while ((ch = unc.read()) >= 0) {
                baos.write(ch);
            }
        } else if (message instanceof  PGPOnePassSignatureList) {
            throw new PGPException("Encrypted message contains a signed message - not literal data.");
        } else {
            throw new PGPException("Message is not a simple encrypted file - type unknown.");
        }

        if (pbe.isIntegrityProtected()) {
            if (!pbe.verify()) {
                throw new PGPException("Message failed integrity check");
            }
        }
        return baos.toByteArray();
    }

    private byte[] sign(byte[] contentToSign, String alias, char[] passphrase) throws IOException, PGPException {

        PGPSecretKey secretKey = getSecretKey(alias, passphrase);
        if(secretKey == null) {
            LOG.warning("Secret Key not found for alias.");
            return null;
        }

        PGPPrivateKey privateKey = getPrivateKey(secretKey, passphrase);
        if(privateKey == null) {
            LOG.warning("Private Key not found for secret key.");
            return null;
        }
        PGPSignatureGenerator sGen = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(
                        secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1).setProvider(PROVIDER_BOUNCY_CASTLE));
        sGen.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ArmoredOutputStream aOut = new ArmoredOutputStream(byteOut);
        BCPGOutputStream bOut = new BCPGOutputStream(byteOut);

        sGen.update(contentToSign);

        aOut.endClearText();
        sGen.generate().encode(bOut);
        aOut.close();

        return byteOut.toByteArray();
    }

    private boolean verifySignature(byte[] contentSigned, byte[] signature, String alias) throws IOException, PGPException {
        PGPObjectFactory pgpFact = new PGPObjectFactory(signature, new BcKeyFingerprintCalculator());
        PGPSignatureList p3 = null;
        Object o = pgpFact.nextObject();
        if (o instanceof PGPCompressedData) {
            PGPCompressedData c1 = (PGPCompressedData)o;
            pgpFact = new PGPObjectFactory(c1.getDataStream(), new BcKeyFingerprintCalculator());
            p3 = (PGPSignatureList)pgpFact.nextObject();
        } else {
            p3 = (PGPSignatureList)o;
        }

        PGPSignature sig = p3.get(0);

        PGPPublicKey publicKey = getFirstPublicKey(alias);
        if(publicKey == null) {
            LOG.warning("Unable to find public key to verify signature.");
            return false;
        }
        sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider(PROVIDER_BOUNCY_CASTLE), publicKey);

        sig.update(contentSigned);

        return sig.verify();
    }

    private PGPPrivateKey getPrivateKey(PGPSecretKey secretKey, char[] pass) throws PGPException {
        return secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(PROVIDER_BOUNCY_CASTLE).build(pass));
    }

    private PGPSecretKey getSecretKey(long keyId) {
        Iterator<PGPSecretKey> i = secretKeyRing.getSecretKeys();
        PGPSecretKey k;
        while(i.hasNext()) {
            k = i.next();
            if(keyId == k.getKeyID())
                return k;
        }
        return null;
    }

    private PGPSecretKey getSecretKey(String alias, char[] pass) {
        Iterator<PGPSecretKey> i = secretKeyRing.getSecretKeys();
        while(i.hasNext()) {
            PGPSecretKey secretKey = i.next();
            if(containsAlias(secretKey.getPublicKey(), alias)) {
                return secretKey;
            }
        }
        return null;
    }

    private boolean containsAlias(PGPPublicKey k, String alias) {
        Iterator<String> i = k.getUserIDs();
        while(i.hasNext()) {
            if(i.next().equals(alias))
                return true;
        }
        return false;
    }

    private PGPKeyRingGenerator generateKeyRingGenerator(String alias, char[] passphrase, int s2kCount) {
        PGPKeyRingGenerator keyRingGen = null;
        try {
            RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
            /*
             * This value should be a Fermat number. 0x10001 (F4) is current recommended value. 3 (F1) is known to be safe also.
             * 3, 5, 17, 257, 65537, 4294967297, 18446744073709551617,
             * <p>
             * Practically speaking, Windows does not tolerate public exponents which do not fit in a 32-bit unsigned integer.
             * Using e=3 or e=65537 works "everywhere".
             * <p>
             * See: <a href="http://stackoverflow.com/questions/11279595/rsa-public-exponent-defaults-to-65537-what-should-this-value-be-what-are-the">stackoverflow: RSA Public exponent defaults to 65537. ... What are the impacts of my choices?</a>
             */
            BigInteger publicExponent = BigInteger.valueOf(0x10001);

            /*
             * As of 2018: 2048 is common value - safe until 2030, 3072 is uncommon - safe until 2040, 4096 is rare - safe until 2040+
             */
            int bitStrength = 4096;

            /*
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
//            PGPDigestCalculator sha512Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA512);

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
