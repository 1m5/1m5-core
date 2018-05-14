package io.onemfive.core.sensors.i2p.bote.folder;

import io.onemfive.core.sensors.i2p.bote.UniqueId;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.i2p.util.Log;
import net.i2p.util.SecureFileOutputStream;

/**
 * Email packets are sometimes delivered again after the email has already
 * been received, because some storage nodes were offline the first time.
 * This class stores message IDs of received emails to avoid this problem.
 *
 * File format: one message ID per line, sorted by the time the email was first
 * assembled, oldest to newest.
 *
 * @see IncompleteEmailFolder
 */
public class MessageIdCache {
    private Log log = new Log(MessageIdCache.class);
    private File cacheFile;
    private int cacheSize;
    private List<UniqueId> idList;

    public MessageIdCache(File cacheFile, int sizecacheSize) {
        this.cacheFile = cacheFile;
        this.cacheSize = sizecacheSize;
        read(cacheFile);
    }

    private void read(File cacheFile) {
        idList = Collections.synchronizedList(new ArrayList<UniqueId>());
        if (!cacheFile.exists()) {
            log.debug("Message ID cache file doesn't exist: <" + cacheFile.getAbsolutePath() + ">");
            return;
        }

        log.debug("Reading message ID cache file: <" + cacheFile.getAbsolutePath() + ">");
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(cacheFile));

            while (true) {
                String idString = input.readLine();
                if (idString == null)   // EOF
                    break;

                UniqueId id = new UniqueId(idString);
                idList.add(id);
            }

        }
        catch (IOException e) {
            log.error("Can't read message ID cache file.", e);
        }
        finally {
            if (input != null)
                try {
                    input.close();
                }
                catch (IOException e) {
                    log.error("Error closing BufferedReader.", e);
                }
        }
    }

    private void write(File cacheFile) {
        log.debug("Writing message ID cache file: <" + cacheFile.getAbsolutePath() + ">");
        String newLine = System.getProperty("line.separator");
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new SecureFileOutputStream(cacheFile.getAbsolutePath())));
            for (UniqueId id: idList)
                writer.write(id.toBase64() + newLine);
        }
        catch (IOException e) {
            log.error("Can't write message ID cache file.", e);
        }
        finally {
            if (writer != null)
                try {
                    writer.close();
                }
                catch (IOException e) {
                    log.error("Error closing Writer.", e);
                }
        }
    }

    void add(UniqueId messageId) {
        while (idList.size() > cacheSize)
            idList.remove(0);
        idList.add(messageId);
        // should not be too big a performance hit to write out the cache here because
        // this method only gets called when a genuinely new email is received
        write(cacheFile);
    }

    boolean contains(UniqueId messageId) {
        return idList.contains(messageId);
    }
}
