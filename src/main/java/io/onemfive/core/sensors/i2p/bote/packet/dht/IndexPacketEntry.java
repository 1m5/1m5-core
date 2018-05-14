package io.onemfive.core.sensors.i2p.bote.packet.dht;

import net.i2p.data.Hash;

/**
 * One entry in an {@link IndexPacket}.
 */
public class IndexPacketEntry {
    public Hash emailPacketKey;
    public long storeTime;   // milliseconds since 1-1-1970

    Hash delVerificationHash;

    /**
     * Constructs an <code>IndexPacketEntry</code> with a time stamp of 0.
     * @param emailPacketKey
     * @param delVerificationHash
     */
    IndexPacketEntry(Hash emailPacketKey, Hash delVerificationHash) {
        this(emailPacketKey, delVerificationHash, 0);
    }

    IndexPacketEntry(Hash emailPacketKey, Hash delVerificationHash, long storeTime) {
        this.emailPacketKey = emailPacketKey;
        this.delVerificationHash = delVerificationHash;
        this.storeTime = storeTime;
    }
}
