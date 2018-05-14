package io.onemfive.core.sensors.i2p.bote.network;

import io.onemfive.core.sensors.i2p.bote.packet.dht.DhtStorablePacket;

import java.util.Iterator;

import net.i2p.data.Hash;

/**
 * Defines methods for accessing a local DHT store.
 */
public interface DhtStorageHandler {

    void store(DhtStorablePacket packetToStore);

    /** Retrieves a packet by DHT key. If no matching packet is found, <code>null</code> is returned. */
    DhtStorablePacket retrieve(Hash dhtKey);

    /** Returns all stored packets in the smallest possible units */
    Iterator<? extends DhtStorablePacket> individualPackets();
}
