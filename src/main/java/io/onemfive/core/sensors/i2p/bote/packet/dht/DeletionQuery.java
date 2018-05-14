package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.packet.CommunicationPacket;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * A query to a peer about whether that peer has deleted a given email packet from its DHT store.
 */
@TypeCode('Y')
public class DeletionQuery extends CommunicationPacket {
    private Log log = new Log(DeletionQuery.class);
    private Hash dhtKey;

    /**
     * @param dhtKey The DHT key of the email packet
     */
    public DeletionQuery(Hash dhtKey) {
        this.dhtKey = dhtKey;
    }

    public DeletionQuery(byte[] data) {
        super(data);
        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        dhtKey = readHash(buffer);

        if (buffer.hasRemaining())
            log.debug("Deletion Query has " + buffer.remaining() + " extra bytes.");
    }

    /**
     * Returns the DHT key of the email packet.
     */
    public Hash getDhtKey() {
        return dhtKey;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            writeHeader(outputStream);
            outputStream.write(dhtKey.toByteArray());
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }

        return outputStream.toByteArray();
    }
}
