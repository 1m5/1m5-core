package io.onemfive.core.sensors.i2p.bote.folder;

/** Listens to elements being added or removed from a {@link EmailFolder}. */
public interface FolderListener {

    void elementAdded(String messageId);

    void elementUpdated();

    void elementRemoved(String messageId);
}
