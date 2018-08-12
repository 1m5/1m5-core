package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.util.List;

public class StorePublicKeysRequest extends ServiceRequest {
    public static int KEYID_REQUIRED = 1;
    public static int PUBLIC_KEYS_LIST_REQUIRED = 2;
    public static int NON_EXISTANT_PUBLIC_KEY_RING_COLLECTION = 3;
    public static int NON_EXISTANT_PUBLIC_KEY_RING = 4;

    public long keyId;
    public List<PGPPublicKey> publicKeys;
}
