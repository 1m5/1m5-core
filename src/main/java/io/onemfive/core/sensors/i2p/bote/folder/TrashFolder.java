package io.onemfive.core.sensors.i2p.bote.folder;

import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordHolder;

import java.io.File;

/**
 * Subclassed for distinction between folders that move emails to
 * the trash, and the trash folder which deletes them permanently.
 */
public class TrashFolder extends EmailFolder {

    public TrashFolder(File storageDir, PasswordHolder passwordHolder) {
        super(storageDir, passwordHolder);
    }
}
