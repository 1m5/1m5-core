package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.util.List;

public class GetPublicKeyRequest extends ServiceRequest {
    public String alias;
    // Response
    List<PGPPublicKey> publicKeys;
}
