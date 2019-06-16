package io.onemfive.core.keyring;

import io.onemfive.data.content.Content;

/**
 * Request:
 * String Key Ring Username
 * String Key Ring Passphrase for access to Public Key Ring
 * String Public Key Alias for encryption
 * @see Content#body
 *
 * Response:
 * @see Content#body
 *
 * @since 0.6.1
 * @author objectorange
 */
public class DecryptRequest extends KeyRingsRequest {

    public static int CONTENT_TO_DECRYPT_REQUIRED = 2;
    public static int PUBLIC_KEY_ALIAS_REQUIRED = 3;
    public static int PUBLIC_KEY_NOT_FOUND = 4;
    public static int LOCATION_REQUIRED = 5;
    public static int LOCATION_INACCESSIBLE = 6;

    public String location;
    public String keyRingUsername;
    public String keyRingPassphrase;
    public String alias;
    public Content content;
    public Boolean passphraseOnly = false;
}
