package io.onemfive.core.keyring;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.io.IOException;
import java.util.List;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class SmartPGPKeyRing extends OpenPGPKeyRing {

    @Override
    public void loadKeyRings(LoadKeyRingsRequest request) throws IOException, PGPException {
        super.loadKeyRings(request);
    }

    @Override
    public void saveKeyRings() {
        super.saveKeyRings();
    }

    @Override
    public void generateKeyRings(String alias, char[] passphrase, int hashStrength) throws IOException, PGPException {
        super.generateKeyRings(alias, passphrase, hashStrength);
    }

    @Override
    public void storePublicKeys(StorePublicKeysRequest r) throws PGPException {
        super.storePublicKeys(r);
    }

    @Override
    public PGPPublicKey getPublicKey(String alias, boolean master) throws PGPException {
        return getPublicKey(alias, master);
    }

    @Override
    public PGPPublicKey getPublicKey(String keyRingAlias, String keyAlias) throws PGPException {
        return super.getPublicKey(keyRingAlias, keyAlias);
    }

    @Override
    public PGPPublicKey getPublicKey(byte[] fingerprint) throws PGPException {
        return super.getPublicKey(fingerprint);
    }

    @Override
    public void encrypt(EncryptRequest r) throws IOException, PGPException {
        super.encrypt(r);
    }

    @Override
    public void decrypt(DecryptRequest r) throws IOException, PGPException {
        super.decrypt(r);
    }

    @Override
    public void sign(SignRequest r) throws IOException, PGPException {
        super.sign(r);
    }

    @Override
    public void verifySignature(VerifySignatureRequest r) throws IOException, PGPException {
        super.verifySignature(r);
    }

    @Override
    public boolean containsAlias(PGPPublicKey k, String alias) {
        return super.containsAlias(k, alias);
    }
}
