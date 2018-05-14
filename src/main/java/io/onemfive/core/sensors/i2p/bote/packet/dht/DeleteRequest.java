package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.packet.CommunicationPacket;
import io.onemfive.core.sensors.i2p.bote.packet.I2PBotePacket;

import java.util.Collection;

import net.i2p.data.Hash;

/**
 * Superclass for delete requests. A delete request contains zero or more entries,
 * each consisting of a DHT key (of the DHT item that is to be deleted) and
 * authentication data (which is defined in subclasses).
 */
public abstract class DeleteRequest extends CommunicationPacket {

    protected DeleteRequest() {
    }

    protected DeleteRequest(byte[] data) {
        super(data);
    }

    public abstract Class<? extends I2PBotePacket> getDataType();

    /** Returns all DHT keys in the <code>DeleteRequest</code>. */
    public abstract Collection<Hash> getDhtKeys();

    /**
     * Creates a new <code>DeleteRequest</code> containing only one of the entries in this <code>DeleteRequest</code>.
     * @param dhtKey The DHT key of the entry to use
     * @return A new <code>DeleteRequest</code>, or <code>null</code> if the DHT key doesn't exist in the packet
     */
    public abstract DeleteRequest getIndividualRequest(Hash dhtKey);
}
