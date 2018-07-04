package io.onemfive.core;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface ServiceStatusListener {
    void serviceStatusChanged(String serviceFullName, ServiceStatus serviceStatus);
}
