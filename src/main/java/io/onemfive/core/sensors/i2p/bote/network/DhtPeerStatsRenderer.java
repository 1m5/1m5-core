package io.onemfive.core.sensors.i2p.bote.network;

/**
 * A factory for creating
 */
public interface DhtPeerStatsRenderer {

    String translateHeading(DhtPeerStats.Columns column);

    String translateContent(DhtPeerStats.Content content);
}
