package io.onemfive.core.sensors.i2p.bote.service;

import io.onemfive.core.sensors.i2p.bote.Configuration;
import io.onemfive.core.sensors.i2p.bote.UniqueId;
import io.onemfive.core.sensors.i2p.bote.email.Email;
import io.onemfive.core.sensors.i2p.bote.email.EmailMetadata;
import io.onemfive.core.sensors.i2p.bote.email.EmailMetadata.PacketInfo;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.folder.EmailFolder;
import io.onemfive.core.sensors.i2p.bote.folder.FolderIterator;
import io.onemfive.core.sensors.i2p.bote.network.DHT;
import io.onemfive.core.sensors.i2p.bote.network.NetworkStatusSource;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import net.i2p.util.I2PAppThread;
import net.i2p.util.Log;

/**
 * Periodically sends <code>DeletionQueries</code> for sent email packets and
 * updates the email's delivery status.
 * @see EmailMetadata
 */
public class DeliveryChecker extends I2PAppThread {
    private Log log = new Log(DeliveryChecker.class);
    private DHT dht;
    private EmailFolder sentFolder;
    private Configuration configuration;
    private NetworkStatusSource networkStatusSource;

    public DeliveryChecker(DHT dht, EmailFolder sentFolder, Configuration configuration, NetworkStatusSource networkStatusSource) {
        super("DeliveryChkr");
        this.dht = dht;
        this.sentFolder = sentFolder;
        this.configuration = configuration;
        this.networkStatusSource = networkStatusSource;
        setPriority(MIN_PRIORITY);
    }

    @Override
    public void run() {
        try {
            while (!networkStatusSource.isConnected())
                TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            return;
        }

        while (!Thread.interrupted())
            try {
                try {
                    if (configuration.isDeliveryCheckEnabled()) {
                        log.debug("Processing sent emails in directory '" + sentFolder.getStorageDirectory() + "'.");
                        FolderIterator<Email> iterator = sentFolder.iterate();
                        while (iterator.hasNext()) {
                            Email email = iterator.next();
                            if (!email.getMetadata().isDelivered())
                                checkDelivery(email);
                        }
                    }
                } finally {
                    TimeUnit.MINUTES.sleep(configuration.getDeliveryCheckInterval());
                }
            } catch (InterruptedException e) {
                break;
            } catch (PasswordException e) {
                log.debug("Can't scan sent folder because password is not cached.");
            } catch (RuntimeException e) {   // catch unexpected exceptions to keep the thread running
                log.error("Exception caught in DeliveryChecker loop", e);
            }
    }

    /**
     * Checks the DHT for all undelivered packets belonging to a given email.
     * @param email
     * @throws InterruptedException
     */
    private void checkDelivery(Email email) throws InterruptedException {
        EmailMetadata metadata = email.getMetadata();
        Collection<PacketInfo> packets = metadata.getUndeliveredPacketKeys();
        synchronized(sentFolder) {
            boolean updateMetadata = false;

            for (PacketInfo packet: packets) {
                UniqueId delAuth = dht.findDeleteAuthorizationKey(packet.dhtKey, packet.delVerificationHash);
                if (delAuth != null) {
                    metadata.setPacketDelivered(packet.dhtKey, true);
                    updateMetadata = true;
                    log.debug("Delivery of email packet with DHT key " + packet.dhtKey + " confirmed.");
                }
            }

            if (updateMetadata)
                try {
                    sentFolder.saveMetadata(email);
                } catch (Exception e) {
                    log.error("Can't save email metadata.", e);
                }
        }
    }
}
