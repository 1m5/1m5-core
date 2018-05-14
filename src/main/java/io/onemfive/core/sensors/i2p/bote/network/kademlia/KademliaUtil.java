package io.onemfive.core.sensors.i2p.bote.network.kademlia;

import java.math.BigInteger;

import net.i2p.data.DataHelper;
import net.i2p.data.Hash;

class KademliaUtil {

    /**
     * Calculates the Kademlia distance (XOR distance) between two hashes.
     * If the hashes are equal, the distance is zero; otherwise, it is greater
     * than zero.
     * @param key1
     * @param key2
     */
    static BigInteger getDistance(Hash key1, Hash key2) {
        // This shouldn't be a performance bottleneck, so save some mem by not using Hash.cachedXor
        byte[] xoredData = DataHelper.xor(key1.getData(), key2.getData());
        return new BigInteger(1, xoredData);
    }
}
