package io.onemfive.core.sensors;

import io.onemfive.data.Peer;

import java.util.Map;
import java.util.Properties;

public class SensorManagerSimple implements SensorManager {

    @Override
    public void updatePeer(Peer peer) {

    }

    @Override
    public Map<String, Peer> getAllPeers() {
        return null;
    }

    @Override
    public boolean init(Properties properties) {
        return false;
    }

    @Override
    public boolean shutdown() {
        return false;
    }
}
