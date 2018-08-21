package io.onemfive.core.sensors;

import io.onemfive.core.ServiceRequest;
import io.onemfive.data.DID;

public class SensorRequest extends ServiceRequest {
    public static int TO_PEER_REQUIRED = 1;
    public static int TO_PEER_WRONG_NETWORK = 2;
    public static int NO_CONTENT = 3;
    public static int TO_PEER_NOT_FOUND = 4;

    public DID from;
    public DID to;
    public Object content;
}
