package io.onemfive.core.did.crypto;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Implements {@link #toByteArray(PublicKeyPair)} and {@link #toByteArray(PrivateKeyPair)},
 * and provides methods for AES encryption and decryption.
 *
 * Originally from I2P-Bote.
 */
public abstract class BaseCrypto implements Crypto {

    private Cipher aesCipher;

    protected BaseCrypto() throws GeneralSecurityException {
        try {
            aesCipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        } catch (NoSuchPaddingException e) {
            // SUN provider incorrectly calls it PKCS5Padding
            aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
    }
    
    /** This implementation returns the whole set of Base64 characters. */
    @Override
    public String getBase64InitialCharacters() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    }
    
    @Override
    public byte[] toByteArray(PublicKeyPair keyPair) {
        byte[] encKey = keyPair.encryptionKey.getEncoded();
        byte[] sigKey = keyPair.signingKey.getEncoded();
        byte[] encodedKeys = new byte[encKey.length + sigKey.length];
        System.arraycopy(encKey, 0, encodedKeys, 0, encKey.length);
        System.arraycopy(sigKey, 0, encodedKeys, encKey.length, sigKey.length);
        return encodedKeys;
    }
    
    @Override
    public byte[] toByteArray(PrivateKeyPair keyPair) {
        byte[] encKey = keyPair.encryptionKey.getEncoded();
        byte[] sigKey = keyPair.signingKey.getEncoded();
        byte[] encodedKeys = new byte[encKey.length + sigKey.length];
        System.arraycopy(encKey, 0, encodedKeys, 0, encKey.length);
        System.arraycopy(sigKey, 0, encodedKeys, encKey.length, sigKey.length);
        return encodedKeys;
    }

    protected byte[] encryptAES(byte[] data, byte[] key, byte[] iv) throws GeneralSecurityException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivps = new IvParameterSpec(iv, 0, 16);
        aesCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivps, new SecureRandom(new byte[43]));

        byte[] encryptedData = new byte[aesCipher.getOutputSize(data.length)];
        int encLen = aesCipher.doFinal(data, 0, data.length, encryptedData, 0);
        byte[] ret = new byte[encLen];
        System.arraycopy(encryptedData, 0, ret, 0, encLen);
        return ret;
    }
    
    protected byte[] decryptAES(byte[] data, byte[] key, byte[] iv) throws GeneralSecurityException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivps = new IvParameterSpec(iv, 0, 16);
        aesCipher.init(Cipher.DECRYPT_MODE, keySpec, ivps, new SecureRandom(new byte[43]));

        byte[] decryptedData = new byte[aesCipher.getOutputSize(data.length)];
        int decLen = aesCipher.doFinal(data, 0, data.length, decryptedData, 0);
        byte[] ret = new byte[decLen];
        System.arraycopy(decryptedData, 0, ret, 0, decLen);
        return ret;
    }
}