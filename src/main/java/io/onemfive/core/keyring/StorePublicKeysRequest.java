package io.onemfive.core.keyring;

import org.bouncycastle.openpgp.PGPPublicKey;

import java.util.List;

public class StorePublicKeysRequest extends KeyRingsRequest {
    public static int KEYID_REQUIRED = 2;
    public static int PUBLIC_KEYS_LIST_REQUIRED = 3;
    public static int NON_EXISTANT_PUBLIC_KEY_RING_COLLECTION = 4;
    public static int NON_EXISTANT_PUBLIC_KEY_RING = 5;

    public long keyId;
    public List<PGPPublicKey> publicKeys;
}
