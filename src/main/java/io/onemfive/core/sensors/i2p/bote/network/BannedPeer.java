package io.onemfive.core.sensors.i2p.bote.network;

import net.i2p.data.Destination;

public class BannedPeer {
    private Destination destination;
    private BanReason banReason;

    public BannedPeer(Destination destination, BanReason banReason) {
        this.destination = destination;
        this.banReason = banReason;
    }

    public Destination getDestination() {
        return destination;
    }

    public BanReason getBanReason() {
        return banReason;
    }
}
