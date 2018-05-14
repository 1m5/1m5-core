package io.onemfive.core.sensors.i2p.bote.network.kademlia;

import java.math.BigInteger;
import java.util.Comparator;

import net.i2p.data.Destination;
import net.i2p.data.Hash;

class PeerDistanceComparator implements Comparator<Destination> {
    private Hash reference;

    PeerDistanceComparator(Hash reference) {
        this.reference = reference;
    }

    @Override
    public int compare(Destination peer1, Destination peer2) {
        BigInteger distance1 = KademliaUtil.getDistance(peer1.calculateHash(), reference);
        BigInteger distance2 = KademliaUtil.getDistance(peer2.calculateHash(), reference);
        return distance1.compareTo(distance2);
    }
}
