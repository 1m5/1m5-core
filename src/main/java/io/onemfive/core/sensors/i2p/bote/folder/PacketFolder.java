package io.onemfive.core.sensors.i2p.bote.folder;

import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.packet.I2PBotePacket;
import io.onemfive.core.sensors.i2p.bote.packet.MalformedPacketException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import net.i2p.util.Log;
import net.i2p.util.SecureFileOutputStream;

/**
 * This class stores new files under a random file name with the .pkt extension.<br/>
 * Unlike {@link Folder}, it is not password-protected and does not throw
 * <code>PasswordException</code>, which is why it can implement <code>Iterable<code>.
 * @param <PacketType> The type of data stored in this folder
 */
public class PacketFolder<PacketType extends I2PBotePacket> extends Folder<PacketType> implements Iterable<PacketType> {
    protected static final String PACKET_FILE_EXTENSION = ".pkt";

    private Log log = new Log(PacketFolder.class);

    public PacketFolder(File storageDir) {
        super(storageDir, PACKET_FILE_EXTENSION);
    }

    /**
     * Saves a packet to a file in the folder. If the file already exists, it is overwritten.
     * @param packetToStore
     * @param filename The filename to store the packet under, relative to this folder's storage directory.
     */
    protected void add(I2PBotePacket packetToStore, String filename) {
        FileOutputStream outputStream = null;
        File file = new File(storageDir, filename);
        try {
            outputStream = new SecureFileOutputStream(file);
            packetToStore.writeTo(outputStream);
        } catch (Exception e) {
            log.error("Can't save packet to file: <" + filename + ">", e);
        }
        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException e) {
                    log.error("Can't close file: <" + filename + ">", e);
                }
                if (file.length() == 0) {
                    log.error("Nothing was written, deleting empty file: <" + file.getAbsolutePath() + ">");
                    file.delete();
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected PacketType createFolderElement(File file) throws IOException {
        try {
            return (PacketType)I2PBotePacket.createPacket(file);
        } catch (MalformedPacketException e) {
            log.error("Found malformed packet, deleting file: " + file.getAbsolutePath() + " (file size=" + file.length() + ")", e);
            if (!file.delete())
                log.error("Can't delete malformed packet");
            return null;
        }
    }

    @Override
    public Iterator<PacketType> iterator() {
        return new Iterator<PacketType>() {
            FolderIterator<PacketType> folderIterator = iterate();

            @Override
            public boolean hasNext() {
                try {
                    return folderIterator.hasNext();
                } catch (PasswordException e) {
                    log.error("Password-encrypted file encountered in a PacketFolder.", e);
                    return false;
                }
            }

            @Override
            public PacketType next() {
                try {
                    return folderIterator.next();
                } catch (PasswordException e) {
                    log.error("Password-encrypted file encountered in a PacketFolder.", e);
                    return null;
                }
            }

            @Override
            public void remove() {
                folderIterator.remove();
            }
        };
    }
}
