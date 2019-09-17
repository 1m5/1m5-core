package io.onemfive.core;

/**
 * Used for updating the ServiceBus.
 *
 * @author objectorange
 */
public interface ServiceStatusListener {
    void serviceStatusChanged(String serviceFullName, ServiceStatus serviceStatus);
}
