package io.onemfive.core.sensors.i2p.bote.network;

import java.net.URL;

/**
 * A resource anchor for the built-in peer file.
 */
public class PeerFileAnchor {

    public static URL getBuiltInPeersFile() {
        return PeerFileAnchor.class.getResource("built-in-peers.txt");
    }
}
