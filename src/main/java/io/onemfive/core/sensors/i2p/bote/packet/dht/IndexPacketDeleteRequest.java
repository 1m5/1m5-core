package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.UniqueId;
import io.onemfive.core.sensors.i2p.bote.packet.I2PBotePacket;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.i2p.data.DataFormatException;
import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * A request to a peer to delete one or more entries from a stored {@link IndexPacket}.<br/>
 * Contains pairs of DHT keys and delete authorization keys.<br/>
 * This class is not thread-safe.
 */
@TypeCode('X')
public class IndexPacketDeleteRequest extends DeleteRequest {
    private Log log = new Log(IndexPacketDeleteRequest.class);
    private Hash emailDestHash;
    private Map<Hash, UniqueId> entries;   // maps DHT keys to delete authorizations

    public IndexPacketDeleteRequest(Hash emailDestHash) {
        this.emailDestHash = emailDestHash;
        entries = new HashMap<Hash, UniqueId>();
    }

    public IndexPacketDeleteRequest(byte[] data) {
        super(data);
        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        emailDestHash = readHash(buffer);
        entries = new HashMap<Hash, UniqueId>();
        int numEntries = buffer.getShort() & 0xFFFF;
        for (int i=0; i<numEntries; i++) {
            Hash dhtKey = readHash(buffer);
            UniqueId delAuthorization = new UniqueId(buffer);
            entries.put(dhtKey, delAuthorization);
        }

        if (buffer.hasRemaining())
            log.debug("Index Packet Delete Request has " + buffer.remaining() + " extra bytes.");
    }

    /**
     * @param dhtKey The Email Packet key to delete
     * @param delAuthorization The delete authorization.
     */
    public void put(Hash dhtKey, UniqueId delAuthorization) {
        entries.put(dhtKey, delAuthorization);
    }

    public Hash getEmailDestHash() {
        return emailDestHash;
    }

    public UniqueId getDeleteAuthorization(Hash dhtKey) {
        return entries.get(dhtKey);
    }

    public int getNumEntries() {
        return entries.size();
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            writeHeader(dataStream);
            emailDestHash.writeBytes(dataStream);
            dataStream.writeShort(entries.size());
            for (Entry<Hash, UniqueId> entry: entries.entrySet()) {
                dataStream.write(entry.getKey().toByteArray());
                dataStream.write(entry.getValue().toByteArray());
            }
        } catch (DataFormatException e) {
            log.error("Invalid format for email destination.", e);
        } catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }

        return byteStream.toByteArray();
    }

    @Override
    public Class<? extends I2PBotePacket> getDataType() {
        return IndexPacket.class;
    }

    @Override
    public Collection<Hash> getDhtKeys() {
        return entries.keySet();
    }

    @Override
    public DeleteRequest getIndividualRequest(Hash dhtKey) {
        UniqueId delAuthorization = entries.get(dhtKey);
        if (delAuthorization == null)
            return null;

        IndexPacketDeleteRequest indivRequest = new IndexPacketDeleteRequest(emailDestHash);
        indivRequest.put(dhtKey, delAuthorization);
        return indivRequest;
    }

    @Override
    public String toString() {
        return super.toString() + " #entries=" + getNumEntries();
    }
}
