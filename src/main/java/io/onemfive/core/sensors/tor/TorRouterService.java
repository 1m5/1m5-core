package io.onemfive.core.sensors.tor;

/**
 * Provides an API for Tor Router.
 * By default, looks for a running Tor instance.
 * If discovered and is configured appropriately, will use it.
 * If discovered and is not configured appropriately, will launch new configured instance.
 * If not found to be installed, will send a message to end user that they need to install Tor (Orbot on Android).
 *
 * @author objectorange
 */
public class TorRouterService {
}
