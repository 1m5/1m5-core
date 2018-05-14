package io.onemfive.core.sensors.i2p.bote.network;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.i2p.data.Destination;

import io.onemfive.core.sensors.i2p.bote.packet.dht.DhtStorablePacket;

public class DhtResults implements Iterable<DhtStorablePacket> {
    private Map<Destination, DhtStorablePacket> map;
    private int totalResponses;

    public DhtResults() {
        map = new ConcurrentHashMap<Destination, DhtStorablePacket>();
        totalResponses = -1;
    }

    public void put(Destination peer, DhtStorablePacket packet) {
        map.put(peer, packet);
    }

    public int getNumResults() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Collection<DhtStorablePacket> getPackets() {
        return map.values();
    }

    public Set<Destination> getPeers() {
        return map.keySet();
    }

    public DhtStorablePacket getPacket(Destination peer) {
        return map.get(peer);
    }

    /**
     * Sets the number of peers that have responded to the DHT lookup.
     * @param totalResponses
     */
    public void setTotalResponses(int totalResponses) {
        this.totalResponses = totalResponses;
    }

    /**
     * Gets the number of peers that have responded to the DHT lookup,
     * including negative responses (those that did not return a data packet).
     */
    public int getTotalResponses() {
        return totalResponses;
    }

    @Override
    public Iterator<DhtStorablePacket> iterator() {
        return getPackets().iterator();
    }
}
