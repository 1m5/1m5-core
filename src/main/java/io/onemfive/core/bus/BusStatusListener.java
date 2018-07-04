package io.onemfive.core.bus;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface BusStatusListener {
    void busStatusChanged(ServiceBus.Status status);
}
