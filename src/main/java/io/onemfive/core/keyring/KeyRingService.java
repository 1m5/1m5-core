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
    private PGPSecretKeyRingCollection secretKeyRingCollection;
    private File pkr;
    // a public key with master key and any sub-keys
    private PGPPublicKeyRingCollection publicKeyRingCollection;

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
                    r = new LoadKeyRingsRequest();
                    r.errorCode = LoadKeyRingsRequest.REQUEST_REQUIRED;
                    DLC.addData(LoadKeyRingsRequest.class, r, e);
                    break;
                }
                if(r.alias == null) {
                    r.errorCode = LoadKeyRingsRequest.ALIAS_REQUIRED;
                    break;
                }
                if(r.passphrase == null) {
                    r.errorCode = LoadKeyRingsRequest.PASSPHRASE_REQUIRED;
                    break;
                }
                if(!r.autoGenerate && r.removeOldKeys) {
                    r.errorCode = LoadKeyRingsRequest.AUTOGENERATE_REMOVE_OLD_KEYS_CONFLICT;
                    r.errorMessage = "If removeOldKeys is set to true, autoGenerate must also be set to true to generate the keys.";
                    break;
                }
                if(r.hashStrength < PASSWORD_HASH_STRENGTH_64) {
                    r.hashStrength = PASSWORD_HASH_STRENGTH_64;
                }
                if(r.secretKeyRingCollectionFileLocation == null) {
                    r.secretKeyRingCollectionFileLocation = "1m5.skr";
                }
                if(r.publicKeyRingCollectionFileLocation == null) {
                    r.publicKeyRingCollectionFileLocation = "1m5.pkr";
                }
                loadKeyRings(r.alias, r.passphrase, r.hashStrength, r.secretKeyRingCollectionFileLocation, r.publicKeyRingCollectionFileLocation, r.autoGenerate, r.removeOldKeys);
                break;
            }
            case OPERATION_SAVE_KEY_RINGS: { saveKeyRings();break; }
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
                generateKeyRings(r.alias, r.passphrase, r.hashStrength);
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
                if(publicKeyRingCollection == null) {
                    r.errorCode = StorePublicKeysRequest.NON_EXISTANT_PUBLIC_KEY_RING_COLLECTION;
                    break;
                }
                try {
                    storePublicKeys(r, r.keyId, r.publicKeys);
                } catch (PGPException e1) {
                    r.exception = e1;
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
                try {
                    if(r.alias != null)
                        r.publicKey = getMasterPublicKey(r.alias);
                    else
                        r.publicKey = getPublicKey(r.fingerprint);
                } catch (PGPException e1) {
                    r.exception = e1;
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
                if(r.alias == null || r.alias.isEmpty()) {
                    r.errorCode = EncryptRequest.ALIAS_REQUIRED;
                    break;
                }
                if(r.textToEncrypt == null || r.textToEncrypt.length == 0) {
                    r.errorCode = EncryptRequest.TEXT_TO_ENCRYPT_REQUIRED;
                    break;
                }
                try {
                    r.encryptedContent = encrypt(r, r.textToEncrypt, r.alias);
                } catch (Exception e1) {
                    r.exception = e1;
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
                try {
                    r.plaintextContent = decrypt(r, r.encryptedContent, r.alias, r.passphrase);
                } catch (Exception e1) {
                    r.exception = e1;
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
                try {
                    r.signature = sign(r, r.contentToSign, r.alias, r.passphrase);
                } catch (Exception e1) {
                    r.exception = e1;
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
                try {
                    r.verified = verifySignature(r, r.contentSigned, r.signature, r.fingerprint);
                } catch (Exception e1) {
                    r.exception = e1;
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
                              boolean autoGenerate,
                              boolean removeOldKeys) {

        boolean newFiles = false;
        skr = new File(secretKeyRingCollectionFileLocation);
        if(removeOldKeys)
            skr.delete();
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
        if(removeOldKeys)
            pkr.delete();
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
                secretKeyRingCollection = new PGPSecretKeyRingCollection(fis, new BcKeyFingerprintCalculator());

                fis = new FileInputStream(pkr);
                publicKeyRingCollection = new PGPPublicKeyRingCollection(fis, new BcKeyFingerprintCalculator());

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (PGPException ex) {
                ex.printStackTrace();
            }
        }

        // If collection could not be loaded then generate them
        if(secretKeyRingCollection == null || publicKeyRingCollection == null && autoGenerate) {
            generateKeyRings(alias, passphrase, hashStrength);
        }
    }

    private void saveKeyRings() {
        // TODO: Encrypt files
        if(secretKeyRingCollection != null && skr != null) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(skr));
                secretKeyRingCollection.encode(bos);
                bos.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }

        if(publicKeyRingCollection != null && pkr != null) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pkr));
                publicKeyRingCollection.encode(bos);
                bos.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
    }

    private void generateKeyRings(String alias, char[] passphrase, int hashStrength) {

        PGPKeyRingGenerator krgen = generateKeyRingGenerator(alias, passphrase, hashStrength);

        // Create and save the Secret Key Ring
        PGPSecretKeyRing secretKeyRing = krgen.generateSecretKeyRing();
        secretKeyRingCollection = PGPSecretKeyRingCollection.addSecretKeyRing(secretKeyRingCollection, secretKeyRing);

        // Create and save the Public Key Ring
        PGPPublicKeyRing publicKeyRing = krgen.generatePublicKeyRing();
        publicKeyRingCollection = PGPPublicKeyRingCollection.addPublicKeyRing(publicKeyRingCollection, publicKeyRing);

        saveKeyRings();
    }

    private void storePublicKeys(StorePublicKeysRequest r, long keyId, List<PGPPublicKey> publicKeys) throws PGPException {
        boolean updated = false;
        PGPPublicKeyRing pkr = publicKeyRingCollection.getPublicKeyRing(keyId);
        PGPPublicKeyRing pkrNew = pkr;
        if(pkr == null) {
            r.errorCode = StorePublicKeysRequest.NON_EXISTANT_PUBLIC_KEY_RING;
            return;
        }
        for (PGPPublicKey k : publicKeys) {
            if(pkrNew.getPublicKey(k.getKeyID()) == null && !k.isMasterKey()) {
                pkrNew = PGPPublicKeyRing.insertPublicKey(pkr, k);
                updated = true;
            }
        }
        if(updated) {
            publicKeyRingCollection = PGPPublicKeyRingCollection.removePublicKeyRing(publicKeyRingCollection, pkr);
            publicKeyRingCollection = PGPPublicKeyRingCollection.addPublicKeyRing(publicKeyRingCollection, pkrNew);
            saveKeyRings();
        }
    }

    private PGPPublicKey getMasterPublicKey(String alias) throws PGPException {
        Iterator<PGPPublicKeyRing> i = publicKeyRingCollection.getKeyRings(alias);
        while(i.hasNext()) {
            PGPPublicKeyRing k = i.next();
            if(k.getPublicKey() != null)
                return k.getPublicKey();
        }
        return null;
    }

    private PGPPublicKey getPublicKey(byte[] fingerprint) throws PGPException {
        return publicKeyRingCollection.getPublicKey(fingerprint);
    }


    private byte[] encrypt(EncryptRequest r, byte[] plainTextContent, String alias) throws IOException, PGPException {

        PGPPublicKey publicKey = getMasterPublicKey(alias);
        if(publicKey == null) {
            r.errorCode = EncryptRequest.PUBLIC_KEY_NOT_FOUND;
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

    private byte[] decrypt(DecryptRequest r, byte[] encryptedContent, String alias, char[] passphrase) throws IOException, PGPException {

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
//            secKey = getSecretKey(pbe.getKeyID());
            secKey = getSecretKey(alias);
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

    private byte[] sign(SignRequest r, byte[] contentToSign, String alias, char[] passphrase) throws IOException, PGPException {

        PGPSecretKey secretKey = getSecretKey(alias);
        if(secretKey == null) {
            r.errorCode = SignRequest.SECRET_KEY_NOT_FOUND;
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

    private boolean verifySignature(VerifySignatureRequest r, byte[] contentSigned, byte[] signature, byte[] fingerprint) throws IOException, PGPException {

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

        PGPPublicKey publicKey = getPublicKey(fingerprint);
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

    private PGPSecretKey getSecretKey(String alias) throws PGPException {

        Iterator<PGPSecretKeyRing> i = secretKeyRingCollection.getKeyRings(alias);
        while(i.hasNext()) {
            PGPSecretKeyRing k = i.next();
            if(k.getSecretKey() != null)
                return k.getSecretKey();
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
            int bitStrength = 2048;

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
            final int certainty = 12;

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
                                    new BcPGPContentSignerBuilder(rsakp_sign.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
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
        boolean isArmored = false;
        String alias = "Alice";
        char[] passphrase = "1234".toCharArray();
        boolean integrityCheck = true;
        int s3kCount = 12;
        String pubKeyFile = "/tmp/1m5/kr/pub.dat";
        String privKeyFile = "/tmp/1m5/kr/secret.dat";

        String plainTextFile = "/tmp/1m5/kr/plain-text.txt"; //create a text file to be encripted, before run the tests
        String cipherTextFile = "/tmp/1m5/kr/cypher-text.dat";
        String decPlainTextFile = "/tmp/1m5/kr/dec-plain-text.txt";
        String signatureFile = "/tmp/s1m5/kr/signature.txt";

        // Load Key Rings
        long start = new Date().getTime();
        s.loadKeyRings(alias, passphrase, PASSWORD_HASH_STRENGTH_64, "skr", "pkr", true, false);
        long end = new Date().getTime();
        long duration = end - start;
        System.out.println("Load KeyRings Duration: "+duration);



        // Generate New Alias Key Ring
        start = new Date().getTime();
        s.generateKeyRings("Bob",passphrase, KeyRingService.PASSWORD_HASH_STRENGTH_64);
        end = new Date().getTime();
        duration = end - start;
        System.out.println("Generate New Alias Key Ring Duration: "+duration);
    }

}
