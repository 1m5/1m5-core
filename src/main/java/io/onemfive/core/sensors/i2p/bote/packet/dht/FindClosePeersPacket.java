package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.packet.CommunicationPacket;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * A request for {@link i2p.bote.network.kademlia.KademliaConstants#K} peers that
 * are closest to a given key.
 */
@TypeCode('F')
public class FindClosePeersPacket extends CommunicationPacket {
    private Log log = new Log(FindClosePeersPacket.class);
    private Hash key;

    public FindClosePeersPacket(Hash key) {
        this.key = key;
    }

    public FindClosePeersPacket(byte[] data) {
        super(data);

        byte[] hashData = new byte[Hash.HASH_LENGTH];
        System.arraycopy(data, CommunicationPacket.HEADER_LENGTH, hashData, 0, hashData.length);
        key = new Hash(hashData);

        int remaining = data.length - (CommunicationPacket.HEADER_LENGTH+hashData.length);
        if (remaining > 0)
            log.debug("Find Close Nodes Request packet has " + remaining + " extra bytes.");
    }
    public Hash getKey() {
        return key;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            writeHeader(outputStream);
            outputStream.write(key.toByteArray());
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }

        return outputStream.toByteArray();
    }

    @Override
    public String toString() {
        return super.toString() + " key=" + key.toBase64().substring(0, 8) + "...";
    }
}
