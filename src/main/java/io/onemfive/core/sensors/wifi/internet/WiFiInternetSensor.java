package io.onemfive.core.sensors.wifi.internet;

import io.onemfive.core.sensors.Sensor;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class WiFiInternetSensor implements Sensor {

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
