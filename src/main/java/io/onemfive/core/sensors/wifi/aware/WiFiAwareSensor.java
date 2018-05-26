package io.onemfive.core.sensors.wifi.aware;

import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.Envelope;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages WiFi Aware for establishing available peers.
 * Not implemented until version 26 / 8.0.
 * Operates in 2.4GHz and 5 GHz.
 *
 * @author objectorange
 */
public class WiFiAwareSensor implements Sensor {

    private static final Logger LOG = Logger.getLogger(WiFiAwareSensor.class.getName());

    @Override
    public boolean send(Envelope envelope) {
        return false;
    }

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
