package io.onemfive.core.sensors.i2p.bote.email;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;

import net.i2p.util.Log;

public class FileAttachment implements Attachment {
    private Log log = new Log(FileAttachment.class);
    private String origFilename;
    private String tempFilename;
    private String mimeType;
    private DataHandler dataHandler;

    public FileAttachment(String origFilename, String tempFilename) {
        this.origFilename = origFilename;
        this.tempFilename = tempFilename;
        loadMimeType();
        dataHandler = new DataHandler(new FileDataSource(tempFilename) {
            @Override
            public String getContentType() {
                return mimeType;
            }
        });
    }

    /**
     * Returns the MIME type for an <code>Attachment</code>. MIME detection is done with
     * JRE classes, so only a small number of MIME types are supported.<p/>
     * It might be worthwhile to use the mime-util library which does a much better job:
     * {@link http://sourceforge.net/projects/mime-util/files/}.
     */
    private void loadMimeType() {
        MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
        mimeType = mimeTypeMap.getContentType(origFilename);
        if (!"application/octet-stream".equals(mimeType))
            return;

        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(tempFilename));
            mimeType = URLConnection.guessContentTypeFromStream(inputStream);
            if (mimeType != null)
                return;
        } catch (IOException e) {
            log.error("Can't read file: <" + tempFilename + ">", e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                log.error("Can't close file: <" + tempFilename + ">", e);
            }
        }

        mimeType = "application/octet-stream";
    }

    @Override
    public String getFileName() {
        return origFilename;
    }

    @Override
    public DataHandler getDataHandler() {
        return dataHandler;
    }

    @Override
    public boolean clean() {
        File tempFile = new File(tempFilename);
        boolean success = tempFile.delete();
        if (!success)
            log.error("Can't delete file: <" + tempFile.getAbsolutePath() + ">");
        return success;
    }
}
