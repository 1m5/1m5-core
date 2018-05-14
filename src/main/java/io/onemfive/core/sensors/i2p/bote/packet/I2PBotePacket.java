package io.onemfive.core.sensors.i2p.bote.packet;

import io.onemfive.core.sensors.i2p.bote.I2PBote;
import io.onemfive.core.sensors.i2p.bote.Util;
import io.onemfive.core.sensors.i2p.bote.packet.dht.DeletionInfoPacket;
import io.onemfive.core.sensors.i2p.bote.packet.dht.DeletionQuery;
import io.onemfive.core.sensors.i2p.bote.packet.dht.Contact;
import io.onemfive.core.sensors.i2p.bote.packet.dht.EmailPacketDeleteRequest;
import io.onemfive.core.sensors.i2p.bote.packet.dht.EncryptedEmailPacket;
import io.onemfive.core.sensors.i2p.bote.packet.dht.FindClosePeersPacket;
import io.onemfive.core.sensors.i2p.bote.packet.dht.IndexPacket;
import io.onemfive.core.sensors.i2p.bote.packet.dht.IndexPacketDeleteRequest;
import io.onemfive.core.sensors.i2p.bote.packet.dht.RetrieveRequest;
import io.onemfive.core.sensors.i2p.bote.packet.dht.StoreRequest;
import io.onemfive.core.sensors.i2p.bote.packet.dht.UnencryptedEmailPacket;
import io.onemfive.core.sensors.i2p.bote.packet.relay.PeerListRequest;
import io.onemfive.core.sensors.i2p.bote.packet.relay.RelayRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.i2p.data.Hash;
import net.i2p.util.Log;

public abstract class I2PBotePacket {
    public static final int MAX_DATAGRAM_SIZE = 10 * 1024;

    private final Log log = new Log(I2PBotePacket.class);

    @SuppressWarnings("unchecked")
    private static Class<? extends I2PBotePacket>[] ALL_PACKET_TYPES = new Class[] {
            RelayRequest.class, ResponsePacket.class, RetrieveRequest.class, StoreRequest.class,
            FindClosePeersPacket.class, PeerListRequest.class, PeerList.class,
            EncryptedEmailPacket.class, UnencryptedEmailPacket.class, EmailPacketDeleteRequest.class,
            IndexPacket.class, IndexPacketDeleteRequest.class, DeletionInfoPacket.class, DeletionQuery.class,
            Contact.class
    };

    private int protocolVersion;

    /**
     * Creates a new <code>I2PBotePacket</code> with the latest protocol version.
     */
    protected I2PBotePacket() {
        protocolVersion = I2PBote.PROTOCOL_VERSION;
    }

    protected I2PBotePacket(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Creates an <code>I2PBotePacket</code> from a file, using the same format as the
     * {@link #createPacket(byte[])} method.
     * @param file
     * @throws MalformedPacketException
     */
    public static I2PBotePacket createPacket(File file) throws MalformedPacketException {
        if (file==null || !file.exists())
            return null;

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            I2PBotePacket packet = createPacket(Util.readBytes(inputStream));
            return packet;
        }
        catch (IOException e) {
            throw new MalformedPacketException("Can't read packet file: " + file.getAbsolutePath(), e);
        }
        finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            }
            catch (IOException e) {
                Log log = new Log(I2PBotePacket.class);
                log.error("Can't close stream.", e);
            }
        }
    }

    /**
     * Creates an <code>I2PBotePacket</code> from its byte array representation.<br/>
     * The header bytes determine whether <code>DataPacket</code> or a <code>CommunicationPacket</code> is created.
     * @param data
     * @throws MalformedPacketException If the byte array does not contain a valid <code>I2PBotePacket</code>.
     */
    private static I2PBotePacket createPacket(byte[] data) throws MalformedPacketException {
        if (CommunicationPacket.isPrefixValid(data))
            return CommunicationPacket.createPacket(data);
        else
            return DataPacket.createPacket(data);
    }

    /**
     * Writes the packet to an <code>OutputStream</code> in binary representation.
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(toByteArray());
    }

    public abstract byte[] toByteArray();

    /**
     * Returns the size of the packet in bytes.
     */
    // TODO rename to getPacketSize
    // TODO override in subclasses to avoid calling toByteArray by adding field lengths
    public int getSize() {
        return toByteArray().length;
    }

    /**
     * Returns <code>false</code> if this packet can't fit into an I2P datagram.
     */
    public boolean isTooBig() {
        return getSize() > MAX_DATAGRAM_SIZE;
    }

    protected char getPacketTypeCode(Class<? extends I2PBotePacket> dataType) {
        return dataType.getAnnotation(TypeCode.class).value();
    }

    public char getPacketTypeCode() {
        return getPacketTypeCode(getClass());
    }

    /**
     * Logs an error if the packet type of the packet instance is not correct
     * @param packetTypeCode
     */
    protected void checkPacketType(byte packetTypeCode) {
        if (getPacketTypeCode() != (char)packetTypeCode)
            log.error("Packet type code of class " + getClass().getSimpleName() + " should be " + getPacketTypeCode() + ", is <" + packetTypeCode + ">");
    }

    /**
     * Returns the version of the I2P-Bote network protocol this packet conforms to.
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Creates a {@link Hash} from bytes read from a {@link ByteBuffer}.
     * No check is done to make sure the buffer has enough bytes available.
     */
    protected Hash readHash(ByteBuffer buffer) {
        byte[] bytes = new byte[Hash.HASH_LENGTH];
        buffer.get(bytes);
        return new Hash(bytes);
    }

    protected static Class<? extends I2PBotePacket> decodePacketTypeCode(char packetTypeCode) {
        for (Class<? extends I2PBotePacket> packetType: ALL_PACKET_TYPES)
            if (packetType.getAnnotation(TypeCode.class).value() == packetTypeCode)
                return packetType;

        Log log = new Log(I2PBotePacket.class);
        log.debug("Invalid type code for I2PBotePacket: <" + packetTypeCode + ">");
        return null;
    }

    /**
     * Returns <code>true</code> if the packet uses a protocol version that is compatible
     * with this I2P-Bote version, <code>false</code> otherwise.
     */
    public boolean isProtocolVersionOk() {
        // everything above 4 is backwards compatible, everything below 4 is incompatible
        return getProtocolVersion() >= 4;
    }

    @Override
    public String toString() {
        return "Type=" + getClass().getSimpleName() + ", code=<" + getPacketTypeCode() + ">, sizeBytes=" + getSize();
    }
}