package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.util.List;

public class StorePublicKeysRequest extends ServiceRequest {
    public List<PGPPublicKey> publicKeys;
}
