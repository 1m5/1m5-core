package io.onemfive.core.sensors.ring;

import io.onemfive.core.sensors.BaseSensor;
import io.onemfive.core.sensors.SensorID;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.Envelope;

import java.util.Properties;

public class RingSensor extends BaseSensor {

    public RingSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    protected SensorID getSensorID() {
        return null;
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
