package io.onemfive.core.sensors;

import io.onemfive.data.Peer;

import java.util.Map;
import java.util.Properties;

/**
 * Uses standard file system access for persisting sensor information.
 */
public class SensorManagerSimple extends SensorManagerBase {

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
