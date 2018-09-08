package io.onemfive.core.sensors.tor;

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
 * Orbot embedded.
 *
 * @author objectorange
 */
public class TorSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(TorSensor.class.getName());

    public TorSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    public Map<String, Peer> getPeers() {
        Map<String, Peer> peers = new HashMap<>();

        return peers;
    }

    @Override
    protected SensorID getSensorID() {
        return SensorID.TOR;
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
