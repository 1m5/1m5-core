package io.onemfive.core.sensors.i2p.bote.network;

import io.onemfive.core.sensors.i2p.bote.packet.CommunicationPacket;

import java.util.EventListener;

import net.i2p.data.Destination;

public interface PacketListener extends EventListener {

    void packetReceived(CommunicationPacket packet, Destination sender, long receiveTime);
}
