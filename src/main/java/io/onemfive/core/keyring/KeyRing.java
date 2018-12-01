package io.onemfive.core.keyring;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface KeyRing {

    void init(Properties properties);

    void loadKeyRings(LoadKeyRingsRequest r) throws IOException, PGPException;

    void saveKeyRings();

    void generateKeyRings(String alias, char[] passphrase, int hashStrength) throws IOException, PGPException;

    void storePublicKeys(StorePublicKeysRequest r) throws PGPException;

    PGPPublicKeyRingCollection getPublicKeyRingCollection();

    PGPPublicKey getPublicKey(String alias, boolean master) throws PGPException;

    PGPPublicKey getPublicKey(String keyRingAlias, String keyAlias) throws PGPException;

    PGPPublicKey getPublicKey(byte[] fingerprint) throws PGPException;

    void encrypt(EncryptRequest r) throws IOException, PGPException;

    void decrypt(DecryptRequest r) throws IOException, PGPException;

    void sign(SignRequest r) throws IOException, PGPException;

    void verifySignature(VerifySignatureRequest r) throws IOException, PGPException;

    boolean containsAlias(PGPPublicKey k, String alias);


}
