package io.onemfive.core.sensors.i2p.bote.network;

import net.i2p.data.Destination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.onemfive.core.sensors.i2p.bote.packet.I2PBotePacket;

import static io.onemfive.core.sensors.i2p.bote.network.BanReason.Reason.WRONG_PROTO_VER;

public class BanList {
    private static BanList instance;
    private Map<Destination, BanReason> bannedPeers;

    public synchronized static BanList getInstance() {
        if (instance == null)
            instance = new BanList();
        return instance;
    }

    private BanList() {
        bannedPeers = new ConcurrentHashMap<Destination, BanReason>();
    }

    private void ban(Destination destination, BanReason reason) {
        bannedPeers.put(destination, reason);
    }

    private void unban(Destination destination) {
        bannedPeers.remove(destination);
    }

    public boolean isBanned(Destination destination) {
        return bannedPeers.containsKey(destination);
    }

    public BanReason getBanReason(Destination destination) {
        return bannedPeers.get(destination);
    }

    public Collection<BannedPeer> getAll() {
        Collection<BannedPeer> peerCollection = new ArrayList<BannedPeer>();
        for (Entry<Destination, BanReason> entry: bannedPeers.entrySet())
            peerCollection.add(new BannedPeer(entry.getKey(), entry.getValue()));
        return peerCollection;
    }

    /**
     * @param peer
     * @param packet A packet received from a peer
     */
    public void update(Destination peer, I2PBotePacket packet) {
        if (packet.isProtocolVersionOk())
            unban(peer);
        else
            ban(peer, new BanReason(WRONG_PROTO_VER, "" + packet.getProtocolVersion()));
    }
}
