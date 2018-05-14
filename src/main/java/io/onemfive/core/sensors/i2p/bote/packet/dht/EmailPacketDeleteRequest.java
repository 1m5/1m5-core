package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.UniqueId;
import io.onemfive.core.sensors.i2p.bote.packet.I2PBotePacket;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;

import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * A request to delete an Email Packet by DHT key
 */
@TypeCode('D')
public class EmailPacketDeleteRequest extends DeleteRequest {
    private Log log = new Log(EmailPacketDeleteRequest.class);
    private Hash dhtKey;
    private UniqueId authorization;

    public EmailPacketDeleteRequest(Hash dhtKey, UniqueId authorization) {
        this.dhtKey = dhtKey;
        this.authorization = authorization;
    }

    public EmailPacketDeleteRequest(byte[] data) {
        super(data);
        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        dhtKey = readHash(buffer);
        authorization = new UniqueId(buffer);

        if (buffer.hasRemaining())
            log.debug("Email Packet Delete Request has " + buffer.remaining() + " extra bytes.");
    }

    public Hash getDhtKey() {
        return dhtKey;
    }

    public UniqueId getAuthorization() {
        return authorization;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            writeHeader(outputStream);
            outputStream.write(dhtKey.toByteArray());
            outputStream.write(authorization.toByteArray());
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }

        return outputStream.toByteArray();
    }

    @Override
    public Class<? extends I2PBotePacket> getDataType() {
        return EncryptedEmailPacket.class;
    }

    @Override
    public Collection<Hash> getDhtKeys() {
        return Collections.singleton(dhtKey);
    }

    @Override
    public DeleteRequest getIndividualRequest(Hash dhtKey) {
        if (this.dhtKey.equals(dhtKey))
            return this;
        else
            return null;
    }

    @Override
    public String toString() {
        return super.toString() + " dhtKey=" + (dhtKey==null?"<null>":dhtKey.toBase64().substring(0, 8)) + "...";
    }
}
