package io.onemfive.core.did;

import io.onemfive.core.ServiceRequest;
import io.onemfive.data.DID;

public class GetLocalDIDRequest extends ServiceRequest {
    public static final int DID_REQUIRED = 1;
    public static final int DID_ALIAS_REQUIRED = 2;
    public static final int DID_PASSPHRASE_REQUIRED = 3;
    public static final int DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN = 4;

    public DID did;
}
