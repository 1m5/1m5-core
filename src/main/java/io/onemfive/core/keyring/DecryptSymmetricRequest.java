package io.onemfive.core.keyring;

import io.onemfive.data.content.Content;

/**
 * Request:
 * @see Content#body (encrypted)
 * @see Content#base64EncodedIV
 * @see Content#encryptionPassphrase
 *
 * Response:
 * @see Content#body (decrypted)
 *
 * @author objectorange
 * @since 0.6.1
 */
public class DecryptSymmetricRequest extends KeyRingsRequest {
    public static int ENCRYPTED_CONTENT_REQUIRED = 2;
    public static int PASSPHRASE_REQUIRED = 3;
    public static int IV_REQUIRED = 4;
    public static int BAD_PASSPHRASE = 5;
    public Content content;
}
