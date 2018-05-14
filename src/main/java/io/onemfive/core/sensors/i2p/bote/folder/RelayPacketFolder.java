package io.onemfive.core.sensors.i2p.bote.folder;

import io.onemfive.core.sensors.i2p.bote.packet.relay.RelayRequest;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import net.i2p.crypto.SHA256Generator;
import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * A <code>PacketFolder</code> that uses filenames that consist of
 * the packet's scheduled send time and the SHA256 hash of the packet.
 */
public class RelayPacketFolder extends PacketFolder<RelayRequest> {
    private final Log log = new Log(RelayPacketFolder.class);

    public RelayPacketFolder(File storageDir) {
        super(storageDir);
    }

    /**
     * Stores a <code>RelayRequest</code> in the folder.
     * @param packet
     */
    public void add(RelayRequest packet) {
        // make the packet's hash part of the filename and don't save if a file with the same hash exists already
        byte[] bytes = packet.toByteArray();
        Hash packetHash = SHA256Generator.getInstance().calculateHash(bytes);
        String base64Hash = packetHash.toBase64();
        if (!fileExistsForHash(base64Hash)) {
            long sendTime = System.currentTimeMillis() + packet.getDelay();
            String filename = sendTime + "_" + base64Hash + PACKET_FILE_EXTENSION;
            add(packet, filename);
            return;
        }
    }

    private boolean fileExistsForHash(final String base64Hash) {
        File[] files = storageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(base64Hash);
            }
        });

        return files.length > 0;
    }

    @Override
    protected RelayRequest createFolderElement(File file) throws IOException {
        RelayRequest packet = super.createFolderElement(file);
        if (packet != null) {
            try {
                long sendTime = getSendTime(file.getName());
                packet.setSendTime(sendTime);
            } catch (NumberFormatException e) {
                log.error("Invalid send time in filename: <" + file.getAbsolutePath() + ">", e);
            }
        }
        return packet;
    }

    private long getSendTime(String filename) throws NumberFormatException {
        String[] parts = filename.split("_");
        return Long.valueOf(parts[0]);
    }
}
