package io.onemfive.core.sensors.i2p.bote.network;

import io.onemfive.core.sensors.i2p.bote.UniqueId;
import io.onemfive.core.sensors.i2p.bote.packet.dht.DhtStorablePacket;
import io.onemfive.core.sensors.i2p.bote.packet.dht.EncryptedEmailPacket;

import java.util.concurrent.CountDownLatch;

import net.i2p.data.Hash;

public interface DHT {

    void store(DhtStorablePacket packet) throws DhtException, InterruptedException;

    DhtResults findOne(Hash key, Class<? extends DhtStorablePacket> dataType) throws InterruptedException;

    DhtResults findAll(Hash key, Class<? extends DhtStorablePacket> dataType) throws InterruptedException;

    /**
     * Returns a Delete Authorization for a DHT key of an {@link EncryptedEmailPacket}, or <code>null</code> if none
     * was found (usually because the Email Packet hasn't been deleted yet).<br/>
     * DHT results are checked against <code>verificationHash</code>, so if a non-null key is returned, it
     * is known to be valid.
     */
    UniqueId findDeleteAuthorizationKey(Hash dhtKey, Hash verificationHash) throws InterruptedException;

    /**
     * Registers a <code>DhtStorageHandler</code> that handles incoming storage requests of a certain
     * type (but not its subclasses).
     * @param packetType
     * @param storageHandler
     */
    void setStorageHandler(Class<? extends DhtStorablePacket> packetType, DhtStorageHandler storageHandler);

    /** Returns a <code>CountDownLatch</code> that switches to zero when a connection to the DHT has been established. */
    CountDownLatch readySignal();

    /** Returns <code>true</code> if a connection to the DHT has been established. */
    boolean isReady();

    DhtPeerStats getPeerStats(DhtPeerStatsRenderer renderer);
}
