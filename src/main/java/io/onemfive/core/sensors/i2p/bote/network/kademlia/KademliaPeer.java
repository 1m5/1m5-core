package io.onemfive.core.sensors.i2p.bote.network.kademlia;

import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;
import net.i2p.data.Hash;
import net.i2p.util.Log;

class KademliaPeer extends Destination {
    private Log log = new Log(KademliaPeer.class);
    private Hash destinationHash;
    private long firstSeen;
    private volatile int consecutiveTimeouts;
    private long lockedUntil;

    KademliaPeer(Destination destination, long lastReception) {
        // initialize the Destination part of the KademliaPeer
        setCertificate(destination.getCertificate());
        setSigningPublicKey(destination.getSigningPublicKey());
        setPublicKey(destination.getPublicKey());

        // initialize KademliaPeer-specific fields
        destinationHash = destination.calculateHash();
        if (destinationHash == null)
            log.error("calculateHash() returned null!");

        firstSeen = lastReception;
    }

    KademliaPeer(Destination destination) {
        this(destination, System.currentTimeMillis());
    }

    KademliaPeer(String peerFileEntry) throws DataFormatException {
        this(new Destination(peerFileEntry));
    }

    public Hash getDestinationHash() {
        return destinationHash;
    }

    /**
     * @param firstSeen Milliseconds since Jan 1, 1970
     * @return
     */
    void setFirstSeen(long firstSeen) {
        this.firstSeen = firstSeen;
    }

    public long getFirstSeen() {
        return firstSeen;
    }

    public int getConsecTimeouts() {
        return consecutiveTimeouts;
    }

    boolean isLocked() {
        return lockedUntil > System.currentTimeMillis();
    }

    /**
     * Locks the peer for 2 minutes after the first timeout, 4 minutes after
     * two consecutive timeouts, 8 minutes after 3 consecutive timeouts, etc.,
     * up to 2^10 minutes (about 17h) for 10 or more consecutive timeouts.
     */
    synchronized void noResponse() {
        consecutiveTimeouts++;
        int lockDuration = 1 << Math.min(consecutiveTimeouts, 10);   // in minutes
        lockedUntil = System.currentTimeMillis() + 60*1000*lockDuration;
    }

    void responseReceived() {
        consecutiveTimeouts = 0;
        lockedUntil = 0;
    }
}
