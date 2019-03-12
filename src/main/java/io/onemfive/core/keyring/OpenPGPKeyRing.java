package io.onemfive.core.keyring;

import io.onemfive.core.util.data.Base64;
import io.onemfive.data.PublicKey;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.*;
import org.bouncycastle.openpgp.operator.jcajce.*;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class OpenPGPKeyRing implements KeyRing {

    private static final Logger LOG = Logger.getLogger(OpenPGPKeyRing.class.getName());

    protected static final String PROVIDER_BOUNCY_CASTLE = "BC";

    protected Properties properties;

    /**
     * Create new Secret and Public Key Ring Collections with a username and passphrase.
     * This equates to creating a new 'account' for one individual to hold
     * all of their key rings. It results in username.skr and username.pkr
     * files with a SecretKeyRing in the skr and a PublicKeyRing in the pkr
     * using the username as the alias providing default keys.
     *
     * Additional identities are created by creating KeyRings with those being
     * added to their collection for persistence.
     * @param r
     * @throws IOException
     * @throws PGPException
     */
    @Override
    public void generateKeyRingCollections(GenerateKeyRingCollectionsRequest r) throws IOException, PGPException {
        LOG.info("Generate Key Rings using OpenPGP request received.");

        File skr = new File(r.keyRingUsername+".skr");
        File pkr = new File(r.keyRingUsername+".pkr");

        // Check to see if key rings collections already exist.
        if(skr.exists()) {
            LOG.warning("KeyRing username taken: "+r.keyRingUsername);
            r.errorCode = GenerateKeyRingCollectionsRequest.KEY_RING_USERNAME_TAKEN;
            return;
        }

        try {
            if(skr.createNewFile() && pkr.createNewFile()) {
                PGPSecretKeyRingCollection secretKeyRingCollection = null;
                PGPPublicKeyRingCollection publicKeyRingCollection = null;

                PGPKeyRingGenerator krgen = generateKeyRingGenerator(r.keyRingUsername, r.keyRingPassphrase.toCharArray(), r.hashStrength);

                // Create and save the Secret Key Ring
                PGPSecretKeyRing secretKeyRing = krgen.generateSecretKeyRing();
                List<PGPSecretKeyRing> pgpSecretKeyRings = new ArrayList<>();
                pgpSecretKeyRings.add(secretKeyRing);
                secretKeyRingCollection = new PGPSecretKeyRingCollection(pgpSecretKeyRings);
                saveSecretKeyRingCollection(secretKeyRingCollection, skr);

                // Create and save the Public Key Ring
                PGPPublicKeyRing publicKeyRing = krgen.generatePublicKeyRing();
                List<PGPPublicKeyRing> pgpPublicKeyRings = new ArrayList<>();
                pgpPublicKeyRings.add(publicKeyRing);
                publicKeyRingCollection = new PGPPublicKeyRingCollection(pgpPublicKeyRings);
                savePublicKeyRingCollection(publicKeyRingCollection, pkr);

                // Now get the identity public key
                PGPPublicKey identityPublicKey = getPublicKey(publicKeyRingCollection, r.keyRingUsername, true);
                if(identityPublicKey != null) {
                    LOG.info("Identity Public Key found.");
                    r.identityPublicKey = new PublicKey();
                    r.identityPublicKey.setAlias(r.keyRingUsername);
                    r.identityPublicKey.setFingerprint(Base64.encode(identityPublicKey.getFingerprint()));
                    r.identityPublicKey.setAddress(Base64.encode(identityPublicKey.getEncoded()));
                    r.identityPublicKey.isIdentityKey(identityPublicKey.isMasterKey());
                    r.identityPublicKey.isEncryptionKey(identityPublicKey.isEncryptionKey());
                }
                // Now get the encryption public key
                PGPPublicKey encryptionPublicKey = getPublicKey(publicKeyRingCollection, r.keyRingUsername, false);
                if(encryptionPublicKey != null) {
                    LOG.info("Encryption Public Key found.");
                    r.encryptionPublicKey = new PublicKey();
                    r.encryptionPublicKey.setAlias(r.keyRingUsername);
                    r.encryptionPublicKey.setFingerprint(Base64.encode(encryptionPublicKey.getFingerprint()));
                    r.encryptionPublicKey.setAddress(Base64.encode(encryptionPublicKey.getEncoded()));
                    r.encryptionPublicKey.isIdentityKey(encryptionPublicKey.isMasterKey());
                    r.encryptionPublicKey.isEncryptionKey(encryptionPublicKey.isEncryptionKey());
                }
            }
            else {
                LOG.warning("Unable to create new key rings.");
            }
        } catch (IOException ex) {
            LOG.warning("IOException caught while saving keys: "+ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Create new key rings both secret and public for a new identity based on alias and aliasPassphrase.
     * @param keyRingUsername
     * @param keyRingPassphrase
     * @param alias
     * @param aliasPassphrase
     * @param hashStrength
     * @throws IOException
     * @throws PGPException
     */
    public void createKeyRings(String keyRingUsername, String keyRingPassphrase, String alias, String aliasPassphrase, int hashStrength) throws IOException, PGPException {
        PGPKeyRingGenerator krgen = generateKeyRingGenerator(alias, aliasPassphrase.toCharArray(), hashStrength);

        PGPSecretKeyRingCollection secretKeyRingCollection = getSecretKeyRingCollection(keyRingUsername, keyRingPassphrase);
        PGPSecretKeyRing secretKeyRing = krgen.generateSecretKeyRing();
        PGPSecretKeyRingCollection.addSecretKeyRing(secretKeyRingCollection, secretKeyRing);
        saveSecretKeyRingCollection(secretKeyRingCollection, new File(keyRingUsername+".skr"));

        PGPPublicKeyRingCollection publicKeyRingCollection = getPublicKeyRingCollection(keyRingUsername, keyRingPassphrase);
        PGPPublicKeyRing publicKeyRing = krgen.generatePublicKeyRing();
        PGPPublicKeyRingCollection.addPublicKeyRing(publicKeyRingCollection, publicKeyRing);
        savePublicKeyRingCollection(publicKeyRingCollection, new File(keyRingUsername+".pkr"));
    }

    /**
     * https://github.com/bcgit/bc-java/blob/master/pg/src/main/java/org/bouncycastle/openpgp/examples/KeyBasedFileProcessor.java
     * @param r
     * @throws IOException
     * @throws PGPException
     */
    @Override
    public void encrypt(EncryptRequest r) throws IOException, PGPException {
        PGPPublicKey publicKey = getPublicKey(getPublicKeyRingCollection(r.keyRingUsername, r.keyRingPassphrase), r.publicKeyAlias, false);
        if(publicKey == null) {
            r.errorCode = EncryptRequest.PUBLIC_KEY_NOT_FOUND;
            return;
        }

        boolean withIntegrityCheck = true;

        // Compress content
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(comData.open(bOut), PGPLiteralData.BINARY, "sec", r.content.getBody().length, new Date());
        pOut.write(r.content.getBody());

        lData.close();
        comData.close();

        // Encrypt content
        JcePGPDataEncryptorBuilder c = new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                .setWithIntegrityPacket(withIntegrityCheck)
                .setSecureRandom(new SecureRandom())
                .setProvider(PROVIDER_BOUNCY_CASTLE);

        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(c);

        JcePublicKeyKeyEncryptionMethodGenerator d = new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
                .setProvider(PROVIDER_BOUNCY_CASTLE)
                .setSecureRandom(new SecureRandom());

        encGen.addMethod(d);

        byte[] bytes = bOut.toByteArray();

        ByteArrayOutputStream content = new ByteArrayOutputStream();

        OutputStream out = new ArmoredOutputStream(content);

        OutputStream cOut = encGen.open(out, bytes.length);

        cOut.write(bytes);

        cOut.close();

        out.close();

        r.content.setBody(content.toByteArray(), false, false);
    }

    /**
     * https://github.com/bcgit/bc-java/blob/master/pg/src/main/java/org/bouncycastle/openpgp/examples/KeyBasedFileProcessor.java
     * @param r
     * @throws IOException
     * @throws PGPException
     */
    @Override
    public void decrypt(DecryptRequest r) throws IOException, PGPException {
        InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(r.content.getBody()));
//        PGPObjectFactory pgpF = new PGPObjectFactory(in, new BcKeyFingerprintCalculator());
        JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
        PGPEncryptedDataList enc;
        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof PGPEncryptedDataList) {
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
        PGPSecretKeyRingCollection pgpSec = getSecretKeyRingCollection(r.keyRingUsername, r.keyRingPassphrase);
        while (privKey == null && it.hasNext()) {
            pbe = it.next();
            privKey = getPrivateKey(pgpSec, pbe.getKeyID(), r.content.getEncryptionPassphrase().toCharArray());
        }

        PublicKeyDataDecryptorFactory b = new JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(PROVIDER_BOUNCY_CASTLE)
                .setContentProvider(PROVIDER_BOUNCY_CASTLE)
                .build(privKey);

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
        r.content.setBody(baos.toByteArray(), false, false);
    }

    @Override
    public void sign(SignRequest r) throws IOException, PGPException {
        PGPSecretKey secretKey = getSecretKey(getSecretKeyRingCollection(r.keyRingUsername, r.keyRingPassphrase), r.alias);
        if(secretKey == null) {
            r.errorCode = SignRequest.SECRET_KEY_NOT_FOUND;
            return;
        }

        PGPPrivateKey privateKey = getPrivateKey(secretKey, r.passphrase.toCharArray());
        if(privateKey == null) {
            LOG.warning("Private Key not found for secret key.");
            return;
        }
        PGPSignatureGenerator sGen = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(
                        secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1).setProvider(PROVIDER_BOUNCY_CASTLE));
        sGen.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ArmoredOutputStream aOut = new ArmoredOutputStream(byteOut);
        BCPGOutputStream bOut = new BCPGOutputStream(byteOut);

        sGen.update(r.contentToSign);

        aOut.endClearText();
        sGen.generate().encode(bOut);
        aOut.close();

        r.signature = byteOut.toByteArray();
    }

    @Override
    public void verifySignature(VerifySignatureRequest r) throws IOException, PGPException {
        PGPObjectFactory pgpFact = new PGPObjectFactory(r.signature, new BcKeyFingerprintCalculator());
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

        PGPPublicKey publicKey = getPublicKey(getPublicKeyRingCollection(r.keyRingUsername,r.keyRingPassphrase),r.fingerprint);
        if(publicKey == null) {
            LOG.warning("Unable to find public key to verify signature.");
            r.verified = false;
            return;
        }
        sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider(PROVIDER_BOUNCY_CASTLE), publicKey);

        sig.update(r.contentSigned);

        r.verified = sig.verify();
    }

    private boolean containsAlias(PGPPublicKey k, String alias) {
        Iterator<String> i = k.getUserIDs();
        while(i.hasNext()) {
            if(i.next().equals(alias))
                return true;
        }
        return false;
    }

    public PGPPublicKeyRingCollection getPublicKeyRingCollection(String username, String passphrase) throws IOException, PGPException {
        // TODO: Decrypt encrypted file
        return new PGPPublicKeyRingCollection(new FileInputStream(username+".pkr"), new BcKeyFingerprintCalculator());
    }

    private void savePublicKeyRingCollection(PGPPublicKeyRingCollection publicKeyRingCollection, File pkr) {
        LOG.info("Persisting Public KeyRing Collection...");
        // TODO: Encrypt file
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

    public PGPPublicKey getPublicKey(PGPPublicKeyRing kr, boolean identity) {
        Iterator<PGPPublicKey> m = kr.getPublicKeys();
        while(m.hasNext()) {
            PGPPublicKey k = m.next();
            if (identity && k.isMasterKey())
                return k;
            else if (!identity && k.isEncryptionKey())
                return k;
        }
        return null;
    }

    public PGPPublicKey getPublicKey(PGPPublicKeyRingCollection c, String keyAlias, boolean identity) throws PGPException {
        Iterator<PGPPublicKeyRing> i = c.getKeyRings(keyAlias);
        PGPPublicKey key = null;
        while(i.hasNext() && key==null) {
            key = getPublicKey(i.next(), identity);
        }
        return key;
    }

    public PGPPublicKey getPublicKey(PGPPublicKeyRingCollection c, String keyAlias) throws PGPException {
        Iterator<PGPPublicKeyRing> i = c.getKeyRings(keyAlias);
        while(i.hasNext()) {
            PGPPublicKeyRing kr = i.next();
            Iterator<PGPPublicKey> m = kr.getPublicKeys();
            while(m.hasNext()) {
                PGPPublicKey k = m.next();
                Iterator<String> u = k.getUserIDs();
                while(u.hasNext()) {
                    String uid = u.next();
                    if(uid.equals(keyAlias))
                        return k;
                }
            }
        }
        return null;
    }

    public PGPPublicKey getPublicKey(PGPPublicKeyRingCollection c, byte[] fingerprint) throws PGPException {
        return c.getPublicKey(fingerprint);
    }

    private PGPSecretKeyRingCollection getSecretKeyRingCollection(String username, String passphrase) throws IOException, PGPException {
//        return new PGPSecretKeyRingCollection(new FileInputStream(username+".skr"), new BcKeyFingerprintCalculator());
        return new PGPSecretKeyRingCollection(new FileInputStream(username+".skr"), new JcaKeyFingerprintCalculator());
    }

    private void saveSecretKeyRingCollection(PGPSecretKeyRingCollection secretKeyRingCollection, File skr) {
        LOG.info("Persisting Secret KeyRing Collection...");
        // TODO: Encrypt file
        if(secretKeyRingCollection != null && skr != null) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(skr));
                secretKeyRingCollection.encode(bos);
                bos.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
    }

    private PGPPrivateKey getPrivateKey(PGPSecretKey secretKey, char[] pass) throws PGPException {
        return secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(PROVIDER_BOUNCY_CASTLE).build(pass));
    }

    private PGPSecretKey getSecretKey(PGPSecretKeyRingCollection c, String alias) throws PGPException {

        Iterator<PGPSecretKeyRing> i = c.getKeyRings(alias);
        while(i.hasNext()) {
            PGPSecretKeyRing k = i.next();
            if(k.getSecretKey() != null)
                return k.getSecretKey();
        }
        return null;
    }

    private PGPPrivateKey getPrivateKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) throws PGPException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
        return getPrivateKey(pgpSecKey, pass);
    }

    private void storePublicKeys(StorePublicKeysRequest r) throws PGPException {
//        boolean updated = false;
//        PGPPublicKeyRing pkr = publicKeyRingCollection.getPublicKeyRing(r.keyId);
//        PGPPublicKeyRing pkrNew = pkr;
//        if(pkr == null) {
//            r.errorCode = StorePublicKeysRequest.NON_EXISTANT_PUBLIC_KEY_RING;
//            return;
//        }
//        for (PGPPublicKey k : r.publicKeys) {
//            if(pkrNew.getPublicKey(k.getKeyID()) == null) {
//                pkrNew = PGPPublicKeyRing.insertPublicKey(pkr, k);
//                updated = true;
//            }
//        }
//        if(updated) {
//            publicKeyRingCollection = PGPPublicKeyRingCollection.removePublicKeyRing(publicKeyRingCollection, pkr);
//            publicKeyRingCollection = PGPPublicKeyRingCollection.addPublicKeyRing(publicKeyRingCollection, pkrNew);
//            saveKeyRings();
//        }
    }

    private PGPKeyRingGenerator generateKeyRingGenerator(String username, char[] passphrase, int s2kCount) {
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
            PGPKeyPair rsaKPSign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());

            // Then an encryption subkey.
            PGPKeyPair rsaKPEncrypt = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());

            // Add a self-signature on the id
            PGPSignatureSubpacketGenerator signHashGen = new PGPSignatureSubpacketGenerator();
            // Add signed metadata on the signature.
            // 1) Declare its purpose
            signHashGen.setKeyFlags(false, KeyFlags.SIGN_DATA|KeyFlags.CERTIFY_OTHER);
            // 2) Set preferences for secondary crypto algorithms to use when sending messages to this key.
            signHashGen.setPreferredSymmetricAlgorithms
                    (false, new int[] {
                            SymmetricKeyAlgorithmTags.AES_256,
                            SymmetricKeyAlgorithmTags.AES_192,
                            SymmetricKeyAlgorithmTags.AES_128
                    });
            signHashGen.setPreferredHashAlgorithms
                    (false, new int[] {
                            HashAlgorithmTags.SHA256,
                            HashAlgorithmTags.SHA1,
                            HashAlgorithmTags.SHA384,
                            HashAlgorithmTags.SHA512,
                            HashAlgorithmTags.SHA224,
                    });
            // 3) Request senders add additional checksums to the
            //    message (useful when verifying unsigned messages.)
            signHashGen.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);

            // Create a signature on the encryption subkey.
            PGPSignatureSubpacketGenerator encryptHashGen = new PGPSignatureSubpacketGenerator();
            // Add metadata to declare its purpose
            encryptHashGen.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS|KeyFlags.ENCRYPT_STORAGE);
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
                            (PGPSignature.POSITIVE_CERTIFICATION, rsaKPSign,
                                    username, sha1Calc, signHashGen.generate(), null,
                                    new BcPGPContentSignerBuilder(rsaKPSign.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
                                    pske);

            // Add our encryption subkey, together with its signature.
            keyRingGen.addSubKey(rsaKPEncrypt, encryptHashGen.generate(), null);
        } catch (PGPException ex) {
            LOG.severe(ex.getLocalizedMessage());
        }
        return keyRingGen;
    }

    @Override
    public void init(Properties properties) {
        this.properties = properties;
    }
}
