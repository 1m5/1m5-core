package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.UniqueId;
import io.onemfive.core.sensors.i2p.bote.packet.DataPacket;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * Contains information about deleted DHT items, which
 * can be Email Packets or Index Packet entries.<br/>
 * Objects of this class are used locally to keep track
 * of deleted packets, and they are sent to peers in
 * response to <code>DeletionQueries</code>.
 * <p/>
 * This class is not thread-safe.
 */
@TypeCode('T')
public class DeletionInfoPacket extends DataPacket implements Iterable<DeletionRecord> {
    private Collection<DeletionRecord> entries;
    private Log log = new Log(DeletionInfoPacket.class);

    public DeletionInfoPacket() {
        entries = new ArrayList<DeletionRecord>();
    }

    public DeletionInfoPacket(byte[] data) {
        super(data);
        entries = new ArrayList<DeletionRecord>();
        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        try {
            int numEntries = buffer.getInt();
            for (int i=0; i<numEntries; i++) {
                Hash dhtKey = readHash(buffer);
                UniqueId delAuthentication = new UniqueId(buffer);
                long storeTime = buffer.getInt() * 1000L;
                DeletionRecord entry = new DeletionRecord(dhtKey, delAuthentication, storeTime);
                entries.add(entry);
            }
        }
        catch (BufferUnderflowException e) {
            log.error("Not enough bytes in packet.", e);
        }

        if (buffer.hasRemaining())
            log.debug("Extra bytes in Index Packet data.");
    }

    /**
     * Adds an entry to the <code>DeletionInfoPacket</code>. If an entry with the same DHT key
     * exists in the packet already, nothing happens.
     * @param dhtKey
     * @param delAuthorization
     */
    public void put(Hash dhtKey, UniqueId delAuthorization) {
        if (contains(dhtKey))
            return;
        DeletionRecord entry = new DeletionRecord(dhtKey, delAuthorization);
        entries.add(entry);
    }

    /**
     * Tests if the <code>DeletionInfoPacket</code> contains a given DHT key.
     * @param dhtKey
     * @return <code>true</code> if the packet containes the DHT key, <code>false</code> otherwise.
     */
    public boolean contains(Hash dhtKey) {
        return getEntry(dhtKey) != null;
    }

    public DeletionRecord getEntry(Hash dhtKey) {
        for (DeletionRecord entry: entries)
            if (entry.dhtKey.equals(dhtKey))
                return entry;
        return null;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            writeHeader(dataStream);

            dataStream.writeInt(entries.size());
            for (DeletionRecord entry: entries) {
                dataStream.write(entry.dhtKey.toByteArray());
                dataStream.write(entry.delAuthorization.toByteArray());
                dataStream.writeInt((int)(entry.storeTime/1000L));   // store as seconds
            }
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream/DataOutputStream.", e);
        }
        return byteStream.toByteArray();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public Iterator<DeletionRecord> iterator() {
        return entries.iterator();
    }
}
