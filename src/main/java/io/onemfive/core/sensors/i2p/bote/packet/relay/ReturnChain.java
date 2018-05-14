package io.onemfive.core.sensors.i2p.bote.packet.relay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.i2p.util.Log;

/**
 * All this class can do at the moment is read bytes from a <code>ByteBuffer</code>
 * and write them back out.
 */
public class ReturnChain {
    private Log log = new Log(ReturnChain.class);
    private byte[] buffer;

    /** Creates an empty return chain. */
    public ReturnChain() {
        buffer = new byte[0];
    }

    public ReturnChain(ByteBuffer input) {
        int length = input.getShort() & 0xFFFF;
        if (length != 0)
            log.error("Length of return chain must be 0 for this protocol version!");
        buffer = new byte[length];
        input.get(buffer);
    }

    public void writeTo(OutputStream output) throws IOException {
        output.write(buffer.length >> 8);
        output.write(buffer.length & 0xFF);
        output.write(buffer);
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            writeTo(byteStream);
        } catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }
        return byteStream.toByteArray();
    }
}