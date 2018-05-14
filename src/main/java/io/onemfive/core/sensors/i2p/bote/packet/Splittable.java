package io.onemfive.core.sensors.i2p.bote.packet;

import java.util.Collection;

/**
 * A {@link DataPacket} that can be broken up into smaller packets
 * if it cannot fit into an I2P datagram.
 */
public interface Splittable {

    /**
     * Splits up this <code>DataPacket</code> so the resulting packets
     * fit in a datagram.
     * @TODO when wrapped into a CommunicationPacket, it still needs to fit
     * @return
     */
    Collection<? extends DataPacket> split();
}
