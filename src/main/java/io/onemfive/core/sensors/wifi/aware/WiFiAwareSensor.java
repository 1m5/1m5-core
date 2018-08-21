package io.onemfive.core.sensors.wifi.aware;

import io.onemfive.core.sensors.BaseSensor;
import io.onemfive.core.sensors.Sensor;
import io.onemfive.core.sensors.SensorID;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.Envelope;
import io.onemfive.data.Peer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages WiFi Aware for establishing available peers.
 * Not implemented until version 26 / 8.0.
 * Operates in 2.4GHz and 5 GHz.
 *
 * @author objectorange
 */
public class WiFiAwareSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(WiFiAwareSensor.class.getName());

    public WiFiAwareSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    protected SensorID getSensorID() {
        return SensorID.WIFIAWARE;
    }

    @Override
    public Map<String, Peer> getPeers() {
        Map<String, Peer> peers = new HashMap<>();

        return peers;
    }

    @Override
    public boolean send(Envelope envelope) {
        return false;
    }

    @Override
    public boolean reply(Envelope envelope) {
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
