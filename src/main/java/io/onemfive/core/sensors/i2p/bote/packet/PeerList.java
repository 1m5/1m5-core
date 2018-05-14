package io.onemfive.core.sensors.i2p.bote.packet;

import io.onemfive.core.sensors.i2p.bote.Util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;
import net.i2p.util.Log;

/**
 * A response to a Find Close Peers Request or a Peer List Request.
 */
@TypeCode('L')
public class PeerList extends DataPacket {
    private Log log = new Log(PeerList.class);
    private Collection<Destination> peers;

    public PeerList(Collection<Destination> peers) {
        this.peers = peers;
    }

    public PeerList(byte[] data) throws DataFormatException {
        super(data);

        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        int numPeers = buffer.getShort() & 0xFFFF;
        peers = new ArrayList<Destination>();
        for (int i=0; i<numPeers; i++) {
            Destination peer = Util.createDestination(buffer);
            peers.add(peer);
        }

        if (buffer.hasRemaining())
            log.debug("Peer List has " + buffer.remaining() + " extra bytes.");
    }

    public Collection<Destination> getPeers() {
        return peers;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(arrayOutputStream);

        try {
            writeHeader(dataStream);
            dataStream.writeShort(peers.size());
            for (Destination peer: peers)
                // write the first 384 bytes (the two public keys)
                // TODO This is NOT compatible with newer key types!
                dataStream.write(peer.toByteArray(), 0, 384);
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }

        return arrayOutputStream.toByteArray();
    }
}
