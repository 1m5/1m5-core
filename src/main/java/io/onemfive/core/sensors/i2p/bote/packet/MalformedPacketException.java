package io.onemfive.core.sensors.i2p.bote.packet;

/**
 * Thown when a packet cannot be decoded from a byte array.
 */
public class MalformedPacketException extends Exception {
    private static final long serialVersionUID = -600763395717614292L;

    public MalformedPacketException(String message) {
        super(message);
    }

    public MalformedPacketException(String message, Throwable cause) {
        super(message, cause);
    }
}
