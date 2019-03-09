package io.onemfive.core.keyring;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class EncryptSymmetricRequest extends KeyRingsRequest {
    public static int CONTENT_TO_ENCRYPT_REQUIRED = 2;
    public static int PASSPHRASE_REQUIRED = 3;
    // Request
    public String passphrase;
    public byte[] contentToEncrypt;
    // Response
    public byte[] encryptedContent;
    public String iv;
}
