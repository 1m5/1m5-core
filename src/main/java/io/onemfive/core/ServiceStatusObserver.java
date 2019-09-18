package io.onemfive.core;

/**
 * Used for updating clients on a specific service's status.
 */
public interface ServiceStatusObserver {
    void statusUpdated(ServiceStatus serviceStatus);
}
