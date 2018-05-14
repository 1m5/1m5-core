package io.onemfive.core.sensors.i2p.bote.network;

import java.util.List;

/**
 * Holds information on a single peer's data, for displaying in the UI.
 */
public interface DhtPeerStatsRow {

    boolean isReachable();

    /**
     * Returns the data in each cell as a String.
     */
    List<String> toStrings();
}
