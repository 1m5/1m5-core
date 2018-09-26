package io.onemfive.core.keyring;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.openpgp.*;
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

    protected File skr;
    // a secret key with master key and any sub-keys
    protected PGPSecretKeyRingCollection secretKeyRingCollection;
    protected File pkr;
    // a public key with master key and any sub-keys
    protected PGPPublicKeyRingCollection publicKeyRingCollection;

    @Override
    public PGPPublicKeyRingCollection getPublicKeyRingCollection() {
        return publicKeyRingCollection;
    }

    @Override
    public void loadKeyRings(String alias, char[] passphrase, int hashStrength, String secretKeyRingCollectionFileLocation, String publicKeyRingCollectionFileLocation, boolean autoGenerate, boolean removeOldKeys) throws IOException, PGPException {
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

    @Override
    public void saveKeyRings() {
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

    @Override
    public void generateKeyRings(String alias, char[] passphrase, int hashStrength) throws IOException, PGPException {

        PGPKeyRingGenerator krgen = generateKeyRingGenerator(alias, passphrase, hashStrength);

        // Create and save the Secret Key Ring
        PGPSecretKeyRing secretKeyRing = krgen.generateSecretKeyRing();
        if(secretKeyRingCollection == null) {
            List<PGPSecretKeyRing> pgpSecretKeyRings = new ArrayList<>();
            pgpSecretKeyRings.add(secretKeyRing);
            secretKeyRingCollection = new PGPSecretKeyRingCollection(pgpSecretKeyRings);
        } else {
            secretKeyRingCollection = PGPSecretKeyRingCollection.addSecretKeyRing(secretKeyRingCollection, secretKeyRing);
        }

        // Create and save the Public Key Ring
        PGPPublicKeyRing publicKeyRing = krgen.generatePublicKeyRing();
        if(publicKeyRingCollection == null) {
            List<PGPPublicKeyRing> pgpPublicKeyRings = new ArrayList<>();
            pgpPublicKeyRings.add(publicKeyRing);
            publicKeyRingCollection = new PGPPublicKeyRingCollection(pgpPublicKeyRings);
        } else {
            publicKeyRingCollection = PGPPublicKeyRingCollection.addPublicKeyRing(publicKeyRingCollection, publicKeyRing);
        }

        saveKeyRings();

    }

    @Override
    public void storePublicKeys(StorePublicKeysRequest r, long keyId, List<PGPPublicKey> publicKeys) throws PGPException {
        boolean updated = false;
        PGPPublicKeyRing pkr = publicKeyRingCollection.getPublicKeyRing(keyId);
        PGPPublicKeyRing pkrNew = pkr;
        if(pkr == null) {
            r.errorCode = StorePublicKeysRequest.NON_EXISTANT_PUBLIC_KEY_RING;
            return;
        }
        for (PGPPublicKey k : publicKeys) {
            if(pkrNew.getPublicKey(k.getKeyID()) == null) {
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

    @Override
    public PGPPublicKey getPublicKey(String alias, boolean master) throws PGPException {
        Iterator<PGPPublicKeyRing> i = publicKeyRingCollection.getKeyRings(alias);
        while(i.hasNext()) {
            PGPPublicKeyRing kr = i.next();
            Iterator<PGPPublicKey> m = kr.getPublicKeys();
            while(m.hasNext()) {
                PGPPublicKey k = m.next();
                if (master && k.isMasterKey())
                    return k;
                else if (!master && k.isEncryptionKey())
                    return k;
            }
        }
        return null;
    }

    @Override
    public PGPPublicKey getPublicKey(String keyRingAlias, String keyAlias) throws PGPException {
        Iterator<PGPPublicKeyRing> i = publicKeyRingCollection.getKeyRings(keyRingAlias);
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

    @Override
    public PGPPublicKey getPublicKey(byte[] fingerprint) throws PGPException {
        return publicKeyRingCollection.getPublicKey(fingerprint);
    }

    @Override
    public byte[] encrypt(EncryptRequest r, byte[] contentToEncrypt, byte[] fingerprint) throws IOException, PGPException {
        PGPPublicKey publicKey = getPublicKey(fingerprint);
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
        OutputStream pOut = lData.open(comData.open(bOut), PGPLiteralData.BINARY, "sec", contentToEncrypt.length, new Date());
        pOut.write(contentToEncrypt);

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

    @Override
    public byte[] decrypt(DecryptRequest r, byte[] encryptedContent, String alias, char[] passphrase) throws IOException, PGPException {
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

    @Override
    public byte[] sign(SignRequest r, byte[] contentToSign, String alias, char[] passphrase) throws IOException, PGPException {
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

    @Override
    public boolean verifySignature(VerifySignatureRequest r, byte[] contentSigned, byte[] signature, byte[] fingerprint) throws IOException, PGPException {
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

    @Override
    public boolean containsAlias(PGPPublicKey k, String alias) {
        Iterator<String> i = k.getUserIDs();
        while(i.hasNext()) {
            if(i.next().equals(alias))
                return true;
        }
        return false;
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
    public void init(Properties properties) {
        this.properties = properties;
    }
}
