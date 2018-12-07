package io.onemfive.core.keyring;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface KeyRing {

    void init(Properties properties);

    void generateKeyRingCollections(GenerateKeyRingCollectionsRequest r) throws IOException, PGPException;

    PGPPublicKeyRingCollection getPublicKeyRingCollection(String username, String passphrase) throws IOException, PGPException;

    PGPPublicKey getPublicKey(PGPPublicKeyRingCollection c, String keyAlias, boolean master) throws PGPException;

    void createKeyRings(String keyRingUsername, String keyRingPassphrase, String alias, String aliasPassphrase, int hashStrength) throws IOException, PGPException;

    void encrypt(EncryptRequest r) throws IOException, PGPException;

    void decrypt(DecryptRequest r) throws IOException, PGPException;

    void sign(SignRequest r) throws IOException, PGPException;

    void verifySignature(VerifySignatureRequest r) throws IOException, PGPException;

}
