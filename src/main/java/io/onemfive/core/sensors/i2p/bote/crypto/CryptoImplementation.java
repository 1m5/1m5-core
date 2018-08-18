package io.onemfive.core.sensors.i2p.bote.crypto;

import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Interface for all encryption/signature algorithm combinations supported by
 * I2P-Bote. Also contains methods for converting between Base64 keys and
 * key objects.
 * <p/>
 * This interface does not define symmetric encryption, which is always AES-256,
 * nor a hash algorithm, which is SHA-256 or SHA-512.
 */
public interface CryptoImplementation {

    /** Returns a user-friendly name for this <code>CryptoImplementation</code>. */
    String getName();

    /** Returns a number that identifies this <code>CryptoImplementation</code>. */
    byte getId();

    /**
     * Returns the number of characters in a Base64 encoding of a pair of public keys (an encryption key and a signing key) -
     * in other words, the length of an Email Destination that uses this <code>CryptoImplementation</code>.
     */
    int getBase64PublicKeyPairLength();

    /**
     * Returns the total number of characters in a Base64 encoding of an encryption key pair and a signing key pair -
     * in other words, the length of an Email Identity that uses this <code>CryptoImplementation</code>.
     */
    int getBase64CompleteKeySetLength();

    int getByteArrayPublicKeyPairLength();

    //
    // Key generation
    //

    KeyPair generateEncryptionKeyPair() throws GeneralSecurityException;

    KeyPair generateSigningKeyPair() throws GeneralSecurityException;

    /** Returns all possible characters that a Base64-encoded Email Destination can init with. */
    String getBase64InitialCharacters();

    //
    // Key conversion
    //

    /**
     * The toByteArray methods are incompatible with the toBase64 methods.
     * Using this method and base64-encoding the byte array may result in longer strings than calling toBase64 directly.
     */
    byte[] toByteArray(PublicKeyPair keyPair);

    /**
     * The toByteArray methods are incompatible with the toBase64 methods.
     * Using this method and base64-encoding the byte array may result in longer strings than calling toBase64 directly.
     */
    byte[] toByteArray(PrivateKeyPair keyPair);

    /**
     * This is the counterpart to {@link #toByteArray(PublicKeyPair)}.
     */
    PublicKeyPair createPublicKeyPair(byte[] bytes) throws GeneralSecurityException;

    /**
     * This is the counterpart to {@link #toByteArray(PrivateKeyPair)}.
     */
    PrivateKeyPair createPrivateKeyPair(byte[] bytes) throws GeneralSecurityException;

    /**
     * The toBase64 methods are incompatible with the toByteArray methods.
     * Using this method may result in shorter strings than calling toByteArray and Base64-encoding the byte array.
     */
    String toBase64(PublicKeyPair keyPair) throws GeneralSecurityException;

    /**
     * Converts a public encryption key to Base64.
     */
    String encryptionKeyToBase64(PublicKey key) throws GeneralSecurityException;

    /**
     * The toBase64 methods are incompatible with the toByteArray methods.
     * Using this method may result in shorter strings than calling toByteArray and Base64-encoding the byte array
     */
    String toBase64(PrivateKeyPair keyPair) throws GeneralSecurityException;

    /**
     * This is the counterpart to {@link #toBase64(PublicKeyPair)}.
     * The toBase64 methods are incompatible with the toByteArray methods.
     * This method may not work with strings obtained by Base64-encoding the result of {@link #toByteArray(PublicKeyPair)}.
     */
    PublicKeyPair createPublicKeyPair(String base64) throws GeneralSecurityException;

    /**
     * This is the counterpart to {@link #toBase64(PrivateKeyPair)}.
     * The toBase64 methods are incompatible with the toByteArray methods.
     * This method may not work with strings obtained by Base64-encoding the result of {@link #toByteArray(PrivateKeyPair)}.
     */
    PrivateKeyPair createPrivateKeyPair(String base64) throws GeneralSecurityException;

    //
    // Encryption and signing
    //

    byte[] encrypt(byte[] data, PublicKey key) throws GeneralSecurityException;

    /** This method takes a public key in addition to the private key because some algorithms need the public key for decryption. */
    byte[] decrypt(byte[] data, PublicKey publicKey, PrivateKey privateKey) throws GeneralSecurityException;

    /**
     * @param data
     * @param privateKey
     * @param keyUpdateHandler Called if the signature algorithm alters the private key
     * @return a signature
     * @throws GeneralSecurityException
     */
    byte[] sign(byte[] data, PrivateKey privateKey, KeyUpdateHandler keyUpdateHandler) throws GeneralSecurityException, PasswordException;

    boolean verify(byte[] data, byte[] signature, PublicKey key) throws GeneralSecurityException;
}
