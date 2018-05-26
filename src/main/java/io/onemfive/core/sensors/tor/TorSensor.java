package io.onemfive.core.sensors.tor;

import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.Envelope;

import java.util.Properties;
import java.util.logging.Logger;

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

    private static final Logger LOG = Logger.getLogger(TorSensor.class.getName());

    @Override
    public boolean send(Envelope envelope) {
        return false;
    }

    @Override
    public boolean start(Properties properties) {

        return true;
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

        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
