package io.onemfive.core.sensors.tor;

import io.onemfive.core.sensors.Sensor;

import java.util.Properties;

/**
 * Provides an API for Tor Router.
 * By default, looks for a running Tor instance.
 * If discovered and is configured appropriately, will use it.
 * If discovered and is not configured appropriately, will launch new configured instance.
 * If not found to be installed, will send a message to end user that they need to install Tor (Orbot on Android).
 *
 * @author objectorange
 */
public class TorSensor implements Sensor {

    @Override
    public boolean start(Properties properties) {
        return false;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean gracefulShutdown() {
        return false;
    }
}
