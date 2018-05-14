package io.onemfive.core.sensors.i2p.bote.network;

import io.onemfive.core.sensors.i2p.bote.Util;
import io.onemfive.core.sensors.i2p.bote.folder.RelayPacketFolder;
import io.onemfive.core.sensors.i2p.bote.packet.CommunicationPacket;
import io.onemfive.core.sensors.i2p.bote.packet.MalformedPacketException;
import io.onemfive.core.sensors.i2p.bote.packet.dht.DhtStorablePacket;
import io.onemfive.core.sensors.i2p.bote.packet.dht.StoreRequest;
import io.onemfive.core.sensors.i2p.bote.packet.relay.RelayRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.i2p.client.I2PSession;
import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;
import net.i2p.util.Log;

/**
 * Receives {@link RelayRequest}s from other peers and forwards them
 * (for {@link RelayRequest} payloads) or stores them in the DHT
 * (for {@link DhtStorablePacket} payloads).
 */
public class RelayPacketHandler implements PacketListener {
    private static final int MAX_CONCURRENT_DHT_TASKS = 5;
    private static final int THREAD_STACK_SIZE = 256 * 1024;

    private Log log = new Log(RelayPacketHandler.class);
    private RelayPacketFolder relayPacketFolder;
    private DHT dht;
    private I2PSendQueue sendQueue;
    private I2PSession i2pSession;
    private ExecutorService dhtTaskExecutor;

    public RelayPacketHandler(RelayPacketFolder relayPacketFolder, DHT dht, I2PSendQueue sendQueue, I2PSession i2pSession) {
        this.relayPacketFolder = relayPacketFolder;
        this.dht = dht;
        this.sendQueue = sendQueue;
        this.i2pSession = i2pSession;
        dhtTaskExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_DHT_TASKS, Util.createThreadFactory("DHTStoreTask", THREAD_STACK_SIZE));
    }

    @Override
    public void packetReceived(CommunicationPacket packet, Destination sender, long receiveTime) {
        if (packet instanceof RelayRequest && dht.isReady()) {
            RelayRequest relayRequest = (RelayRequest)packet;
            CommunicationPacket payload;
            try {
                payload = relayRequest.getStoredPacket(i2pSession);
            }
            catch (DataFormatException e) {
                log.error("Invalid RelayRequest received from peer " + Util.toBase32(sender), e);
                return;
            }
            catch (MalformedPacketException e) {
                log.error("Invalid RelayRequest received from peer " + Util.toBase32(sender), e);
                return;
            }
            log.debug("Received a relay request, payload: " + payload);
            if (payload instanceof RelayRequest) {
                log.debug("Relay packet is of type " + payload.getClass().getSimpleName() + ", storing it in the relay packet folder.");
                relayPacketFolder.add((RelayRequest)payload);
                confirm(sender, relayRequest);
            }
            else if (payload instanceof StoreRequest) {
                log.debug("Relay packet is of type " + payload.getClass().getSimpleName() + ", storing it in the DHT.");
                final DhtStorablePacket dhtPacket = ((StoreRequest)payload).getPacketToStore();
                // do dht.store() in a separate thread so we don't block the notifier thread
                dhtTaskExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dht.store(dhtPacket);
                            log.debug("Finished storing DHT packet: " + dhtPacket);
                        } catch (InterruptedException e) {
                            log.debug("Interrupted while storing packet in the DHT.");
                        } catch (DhtException e) {
                            log.error("Error storing packet in the DHT: " + dhtPacket, e);
                        }
                    }
                });
                confirm(sender, relayRequest);
            }
            else
                log.error("Don't know how to handle relay packet of type " + payload.getClass());
        }
    }

    private void confirm(Destination sender, RelayRequest request) {
        sendQueue.sendResponse(sender, request.getPacketId());
    }
}
