package io.onemfive.core.did;

import io.onemfive.core.ServiceRequest;
import io.onemfive.data.DID;

public class AuthenticateDIDRequest extends ServiceRequest {
    public static final int DID_REQUIRED = 1;
    public static final int DID_ALIAS_REQUIRED = 2;
    public static final int DID_PASSPHRASE_REQUIRED = 3;
    public static final int DID_ALIAS_UNKNOWN = 4;
    public static final int DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN = 5;
    public static final int DID_PASSPHRASE_HASH_ALGORITHM_MISMATCH = 6;
    public static final int DID_PASSPHRASE_MISMATCH = 7;
    public static final int DID_TOKEN_FORMAT_MISMATCH = 8;

    public DID did;
}
