package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.packet.CommunicationPacket;
import io.onemfive.core.sensors.i2p.bote.packet.MalformedPacketException;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import net.i2p.data.Hash;
import net.i2p.util.Log;

import com.nettgryppa.security.HashCash;

/**
 * A request to a peer to store a DHT data item.
 */
@TypeCode('S')
public class StoreRequest extends CommunicationPacket {
    private Log log = new Log(StoreRequest.class);
    private HashCash hashCash;
    private DhtStorablePacket packetToStore;

    public StoreRequest(DhtStorablePacket packetToStore) {
        try {
            hashCash = HashCash.mintCash("", 1);   // TODO
        } catch (NoSuchAlgorithmException e) {
            log.error("Cannot create HashCash.", e);
        }
        this.packetToStore = packetToStore;
    }

    public StoreRequest(byte[] data) throws NoSuchAlgorithmException, MalformedPacketException {
        super(data);
        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        int hashCashLength = buffer.getShort() & 0xFFFF;
        byte[] hashCashData = new byte[hashCashLength];
        buffer.get(hashCashData);
        hashCash = new HashCash(new String(hashCashData));

        int dataLength = buffer.getShort() & 0xFFFF;
        byte[] storedData = new byte[dataLength];
        buffer.get(storedData);
        packetToStore = DhtStorablePacket.createPacket(storedData);

        if (buffer.hasRemaining())
            log.debug("Storage Request Packet has " + buffer.remaining() + " extra bytes.");
    }

    public Hash getKey() {
        return packetToStore.getDhtKey();
    }

    public DhtStorablePacket getPacketToStore() {
        return packetToStore;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteArrayStream);

        try {
            writeHeader(dataStream);
            String hashCashString = hashCash.toString();
            dataStream.writeShort(hashCashString.length());
            dataStream.write(hashCashString.getBytes());
            byte[] dataToStore = packetToStore.toByteArray();
            dataStream.writeShort(dataToStore.length);
            dataStream.write(dataToStore);
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }
        return byteArrayStream.toByteArray();
    }

    @Override
    public String toString() {
        return super.toString() + ", PayldType=" + (packetToStore==null?"<null>":packetToStore.getClass().getSimpleName());
    }
}
