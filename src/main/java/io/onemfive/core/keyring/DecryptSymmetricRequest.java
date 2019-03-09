package io.onemfive.core.keyring;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class DecryptSymmetricRequest extends KeyRingsRequest {
    public static int ENCRYPTED_CONTENT_REQUIRED = 2;
    public static int PASSPHRASE_REQUIRED = 3;
    public static int IV_REQUIRED = 4;
    public static int BAD_PASSPHRASE = 5;
    // Request
    public String passphrase;
    public String iv;
    public byte[] encryptedContent;
    // Response
    public byte[] decryptedContent;
}
