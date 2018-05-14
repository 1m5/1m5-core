package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.UniqueId;
import net.i2p.data.Hash;

/**
 * One entry in a {@link DeletionInfoPacket}.
 */
public class DeletionRecord {
    public Hash dhtKey;
    public UniqueId delAuthorization;
    public long storeTime;   // milliseconds since 1-1-1970

    /**
     * Creates a new {@link DeletionRecord} and sets the store time to the current time.
     * @param dhtKey
     * @param delAuthorization
     */
    public DeletionRecord(Hash dhtKey, UniqueId delAuthorization) {
        this(dhtKey, delAuthorization, System.currentTimeMillis());
    }

    public DeletionRecord(Hash dhtKey, UniqueId delAuthorization, long storeTime) {
        this.dhtKey = dhtKey;
        this.delAuthorization = delAuthorization;
        this.storeTime = storeTime;
    }
}