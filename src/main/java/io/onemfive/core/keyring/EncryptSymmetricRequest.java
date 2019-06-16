package io.onemfive.core.keyring;

import io.onemfive.data.content.Content;

/**
 * Request:
 * @see Content#body
 * @see Content#encryptionPassphrase
 *
 * Response
 * @see Content#body
 * @see Content#base64EncodedIV
 *
 * @since 0.6.1
 * @author objectorange
 */
public class EncryptSymmetricRequest extends KeyRingsRequest {
    public static int CONTENT_TO_ENCRYPT_REQUIRED = 2;
    public static int PASSPHRASE_REQUIRED = 3;

    public Content content;
}
