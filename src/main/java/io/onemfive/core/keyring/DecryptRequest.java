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
    public String keyRingUsername;
    public String keyRingPassphrase;
    public String alias;
    public Content content;
    public Boolean passphraseOnly = false;
}
