package io.onemfive.core.sensors.i2p.bote.email;

/** Listens to identities being added or removed. */
public interface IdentitiesListener {

    void identityAdded(String key);

    void identityUpdated(String key);

    void identityRemoved(String key);
}
