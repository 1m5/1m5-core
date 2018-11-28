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

    }

    @Override
    public void saveKeyRings() {

    }

    @Override
    public void generateKeyRings(String alias, char[] passphrase, int hashStrength) throws IOException, PGPException {

    }

    @Override
    public void storePublicKeys(StorePublicKeysRequest r) throws PGPException {

    }

    @Override
    public PGPPublicKey getPublicKey(String alias, boolean master) throws PGPException {
        return null;
    }

    @Override
    public PGPPublicKey getPublicKey(String keyRingAlias, String keyAlias) throws PGPException {
        return null;
    }

    @Override
    public PGPPublicKey getPublicKey(byte[] fingerprint) throws PGPException {
        return null;
    }

    @Override
    public byte[] encrypt(EncryptRequest r, byte[] contentToEncrypt, byte[] fingerprint) throws IOException, PGPException {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(DecryptRequest r, byte[] encryptedContent, String alias, char[] passphrase) throws IOException, PGPException {
        return new byte[0];
    }

    @Override
    public byte[] sign(SignRequest r, byte[] contentToSign, String alias, char[] passphrase) throws IOException, PGPException {
        return new byte[0];
    }

    @Override
    public boolean verifySignature(VerifySignatureRequest r, byte[] contentSigned, byte[] signature, byte[] fingerprint) throws IOException, PGPException {
        return false;
    }

    @Override
    public boolean containsAlias(PGPPublicKey k, String alias) {
        return false;
    }
}
