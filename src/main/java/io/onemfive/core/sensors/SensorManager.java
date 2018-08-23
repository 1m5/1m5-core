package io.onemfive.core.sensors;

import io.onemfive.data.Peer;

import java.util.Map;
import java.util.Properties;

public interface SensorManager {
    boolean init(Properties properties);
    void updatePeer(Peer peer);
    Map<String,Peer> getAllPeers();
    boolean shutdown();
}
