package io.onemfive.core.sensors.i2p.bote.status;
/**
 * Listens to general status updates from methods.
 */
public interface StatusListener<Status> {

    void updateStatus(Status status, String... args);
}

