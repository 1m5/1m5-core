package io.onemfive.core.sensors.mesh;

import io.onemfive.core.sensors.BaseSensor;
import io.onemfive.core.sensors.Sensor;
import io.onemfive.core.sensors.SensorID;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.Envelope;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author ObjectOrange
 */
public class MeshSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(MeshSensor.class.getName());

    public MeshSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    protected SensorID getSensorID() {
        return SensorID.MESH;
    }

    @Override
    public boolean send(Envelope envelope) {
        return false;
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");

        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean reply(Envelope envelope) {
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
        LOG.info("Shutting down...");

        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
