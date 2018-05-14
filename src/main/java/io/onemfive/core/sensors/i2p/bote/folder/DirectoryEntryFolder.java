package io.onemfive.core.sensors.i2p.bote.folder;

import io.onemfive.core.sensors.i2p.bote.packet.dht.Contact;
import io.onemfive.core.sensors.i2p.bote.packet.dht.DhtStorablePacket;

import java.io.File;
import java.security.GeneralSecurityException;

import net.i2p.util.Log;

/** Stores DHT packets of type {@link Contact}. */
public class DirectoryEntryFolder extends DhtPacketFolder<Contact> {
    private Log log = new Log(DirectoryEntryFolder.class);

    public DirectoryEntryFolder(File storageDir) {
        super(storageDir);
    }

    @Override
    public void store(DhtStorablePacket packetToStore) {
        File packetFile = findPacketFile(packetToStore.getDhtKey());
        if (packetFile != null)
            log.debug("Not storing directory packet with DHT key " + packetToStore.getDhtKey() + " because file exists.");
        else {
            if (!(packetToStore instanceof Contact))
                log.error("Expected class Contact, got " + packetToStore.getClass());
            else {
                Contact contact = (Contact)packetToStore;
                try {
                    if (!contact.verify())
                        log.debug("Not storing Contact because verification failed.");
                    else
                        super.store(packetToStore);
                } catch (GeneralSecurityException e) {
                    log.error("Can't verify Contact", e);
                }
            }
        }
    }
}
