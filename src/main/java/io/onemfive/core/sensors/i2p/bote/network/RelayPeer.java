package io.onemfive.core.sensors.i2p.bote.network;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.i2p.data.Destination;

/**
 * A {@link Destination} that contains information about the most
 * recent attempts to contact the peer, and whether or not it responded.
 */
public class RelayPeer extends Destination {
    private static final int MAX_SAMPLES = 20;   // the maximum size of the samples list

    /**
     * Contains one element for each request sent to the peer.<br/>
     * <code>true</code> means the peer responded to a request, <code>false</code> means
     * no response.<br/>
     * The list is ordered oldest to newest.
     */
    private LinkedList<Boolean> samples;

    /**
     * Creates a new <code>RelayPeer</code> with a given I2P destination and
     * an empty list of reachability data.
     * @param destination
     */
    public RelayPeer(Destination destination) {
        // initialize the Destination part of the RelayPeer
        setCertificate(destination.getCertificate());
        setSigningPublicKey(destination.getSigningPublicKey());
        setPublicKey(destination.getPublicKey());

        // initialize RelayPeer-specific data
        samples = new LinkedList<Boolean>();
    }

    /**
     * Adds information about a new attempt to contact the peer.<br/>
     * @param didRespond <code>true</code> means the peer responded to the request,
     *     <code>false</code> means no response.
     */
    public synchronized void addReachabilitySample(boolean didRespond) {
        samples.add(didRespond);
        while (samples.size() > MAX_SAMPLES)
            samples.removeFirst();
    }

    public List<Boolean> getAllSamples() {
        return Collections.unmodifiableList(samples);
    }

    /**
     * Returns the percentage of requests sent to this peer for which
     * a response was received.<br/>
     * If no request has been sent to the peer yet, <code>0</code> is returned.
     */
    public synchronized int getReachability() {
        int requestsSent = 0;
        int responsesReceived = 0;
        for (boolean didRespond: samples) {
            requestsSent++;
            if (didRespond)
                responsesReceived++;
        }

        if (requestsSent == 0)
            return 0;
        else
            return (int)(100L * responsesReceived / requestsSent);
    }
}
