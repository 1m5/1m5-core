package io.onemfive.core.sensors.i2p.bote.folder;

/** Listens to emails being received. */
public interface NewEmailListener {

    void emailReceived(String messageId);
}
