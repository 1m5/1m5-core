package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.packet.DataPacket;
import io.onemfive.core.sensors.i2p.bote.packet.I2PBotePacket;
import io.onemfive.core.sensors.i2p.bote.packet.MalformedPacketException;

import java.io.File;

import net.i2p.data.Hash;
import net.i2p.util.Log;

public abstract class DhtStorablePacket extends DataPacket {
    private static Log log = new Log(DhtStorablePacket.class);

    protected DhtStorablePacket() {
    }

    /**
     * @see i2p.bote.packet.DataPacket#DataPacket(byte[])
     */
    protected DhtStorablePacket(byte[] data) {
        super(data);
    }

    public abstract Hash getDhtKey();

    /**
     * Creates a {@link DhtStorablePacket} object from its byte array representation.
     * The type of packet depends on the packet type field in the byte array.
     * If there is an error, <code>null</code> is returned.
     * @param data
     * @throws MalformedPacketException
     */
    public static DhtStorablePacket createPacket(byte[] data) throws MalformedPacketException {
        DataPacket packet = DataPacket.createPacket(data);
        if (packet instanceof DhtStorablePacket)
            return (DhtStorablePacket)packet;
        else {
            log.error("Packet is not a DhtStorablePacket: " + packet);
            return null;
        }
    }

    public static Class<? extends DhtStorablePacket> decodePacketTypeCode(char packetTypeCode) {
        Class<? extends I2PBotePacket> packetType = I2PBotePacket.decodePacketTypeCode(packetTypeCode);
        if (packetType!=null && DhtStorablePacket.class.isAssignableFrom(packetType))
            return packetType.asSubclass(DhtStorablePacket.class);
        else {
            log.debug("Invalid type code for DhtStorablePacket: <" + packetTypeCode + ">");
            return null;
        }
    }

    /**
     * Loads a <code>DhtStorablePacket</code> from a file.<br/>
     * Returns <code>null</code> if the file doesn't exist, or if
     * an error occurred.
     * @param file
     * @throws MalformedPacketException
     */
    public static DhtStorablePacket createPacket(File file) throws MalformedPacketException {
        if (file==null || !file.exists())
            return null;

        DataPacket dataPacket;
        dataPacket = DataPacket.createPacket(file);
        if (dataPacket instanceof DhtStorablePacket)
            return (DhtStorablePacket)dataPacket;
        else {
            log.warn("Expected: DhtStorablePacket, got: " + (dataPacket==null?"<null>":dataPacket.getClass().getSimpleName()));
            return null;
        }
    }
}
