package io.onemfive.core.sensors.i2p.bote.packet;

public class EmptyResponse extends DataPacket {

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }
}
