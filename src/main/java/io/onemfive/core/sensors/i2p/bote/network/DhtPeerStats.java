package io.onemfive.core.sensors.i2p.bote.network;

import java.util.List;

/**
 * Holds information on currently known peers in table form,
 * for displaying it on the UI.
 */
public interface DhtPeerStats {

    enum Columns {
        PEER,
        DESTINATION,
        BUCKET_PREFIX,
        DISTANCE,
        LOCKED,
        FIRST_SEEN,
    }

    enum Content {
        YES,
        NO,
        BUCKET_PREFIX_S,
        BUCKET_PREFIX_NONE,
    }

    /**
     * Returns the header row for the table
     */
    List<String> getHeader();

    /**
     * Returns the table data, one row per peer.
     */
    List<DhtPeerStatsRow> getData();
}
