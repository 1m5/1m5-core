package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.packet.CommunicationPacket;
import io.onemfive.core.sensors.i2p.bote.packet.I2PBotePacket;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * A request to a peer to return a data item with a given DHT key and data type.
 */
@TypeCode('Q')
public class RetrieveRequest extends CommunicationPacket {
    private Log log = new Log(RetrieveRequest.class);
    private Hash key;
    private Class<? extends DhtStorablePacket> dataType;

    public RetrieveRequest(Hash key, Class<? extends DhtStorablePacket> dataType) {
        this.key = key;
        this.dataType = dataType;
    }

    public RetrieveRequest(byte[] data) {
        super(data);
        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        char dataTypeCode = (char)buffer.get();
        dataType = DhtStorablePacket.decodePacketTypeCode(dataTypeCode);

        byte[] keyBytes = new byte[Hash.HASH_LENGTH];
        buffer.get(keyBytes);
        key = new Hash(keyBytes);

        if (buffer.hasRemaining())
            log.debug("Retrieve Request Packet has " + buffer.remaining() + " extra bytes.");
    }

    public Hash getKey() {
        return key;
    }

    public Class<? extends I2PBotePacket> getDataType() {
        return dataType;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            writeHeader(outputStream);
            // TODO doesn't the type code already get written in writeHeader?
            outputStream.write(getPacketTypeCode(dataType));
            outputStream.write(key.toByteArray());
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }

        return outputStream.toByteArray();
    }

    @Override
    public String toString() {
        return super.toString() + ", DhtKey=" + key.toBase64() + ", DataType=" + (dataType==null?"<null>":dataType.getSimpleName());
    }
}
