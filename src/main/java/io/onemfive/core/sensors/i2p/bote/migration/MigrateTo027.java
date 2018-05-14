package io.onemfive.core.sensors.i2p.bote.migration;

import io.onemfive.core.sensors.i2p.bote.Configuration;
import io.onemfive.core.sensors.i2p.bote.Util;
import io.onemfive.core.sensors.i2p.bote.fileencryption.EncryptedOutputStream;
import io.onemfive.core.sensors.i2p.bote.fileencryption.FileEncryptionConstants;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordCache;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import net.i2p.util.Log;

/**
 * Migrates email metadata to the encrypted file format.<br/>
 */
class MigrateTo027 {
    private Log log = new Log(MigrateTo027.class);
    private PasswordCache passwordCache;

    /**
     * This method won't corrupt any data if the data has already been migrated to the latest version,
     * because only unencrypted files are converted.
     * @param configuration
     * @throws Exception
     */
    void migrateIfNeeded(Configuration configuration) throws Exception {
        log.debug("Migrating any pre-0.2.7 files...");

        passwordCache = new PasswordCache(configuration);
        // encrypt with the default password
        passwordCache.setPassword(new byte[0]);

        // convert metadata in email folders
        migrateMetadataIfNeeded(configuration.getInboxDir());
        migrateMetadataIfNeeded(configuration.getOutboxDir());
        migrateMetadataIfNeeded(configuration.getSentFolderDir());
        migrateMetadataIfNeeded(configuration.getTrashFolderDir());
    }

    private void migrateMetadataIfNeeded(File directory) throws IOException, PasswordException, GeneralSecurityException {
        if (!directory.exists())
            return;

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".meta");
            }
        };

        for (File file: directory.listFiles(filter))
            if (!isEncrypted(file)) {
                log.debug("Migrating metadata file: <" + file + ">");
                encrypt(file, file);
            }
    }

    private boolean isEncrypted(File file) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] firstFour = new byte[4];
            inputStream.read(firstFour);
            return Arrays.equals(firstFour, FileEncryptionConstants.START_OF_FILE);
        }
        finally {
            if (inputStream != null)
                inputStream.close();
        }
    }

    private void encrypt(File oldFile, File newFile) throws IOException, PasswordException, GeneralSecurityException {
        InputStream inputStream = null;
        byte[] contents = null;
        try {
            inputStream = new FileInputStream(oldFile);
            contents = Util.readBytes(inputStream);
        }
        finally {
            if (inputStream != null)
                inputStream.close();
        }

        EncryptedOutputStream encryptedStream = null;
        try {
            OutputStream fileOutputStream = new FileOutputStream(newFile);
            encryptedStream = new EncryptedOutputStream(fileOutputStream, passwordCache);
            if (contents != null)
                encryptedStream.write(contents);
        }
        finally {
            if (encryptedStream != null)
                encryptedStream.close();
        }
    }
}
