package io.onemfive.core.sensors.nfc;

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
 * TODO: Add Description
 *
 * @author objectorange
 */
public class NFCSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(NFCSensor.class.getName());

    public NFCSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    public Map<String, Peer> getPeers() {
        Map<String, Peer> peers = new HashMap<>();

        return peers;
    }

    @Override
    protected SensorID getSensorID() {
        return SensorID.NFC;
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
