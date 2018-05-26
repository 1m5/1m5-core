package io.onemfive.core.sensors.mesh;

import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.Envelope;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author ObjectOrange
 */
public class MeshSensor implements Sensor {

    private final Logger LOG = Logger.getLogger(MeshSensor.class.getName());

    @Override
    public boolean send(Envelope envelope) {
        return false;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("Starting MeshSensor...");

        System.out.println("MeshSensor started.");
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
        System.out.println("Shutting down MeshSensor...");

        System.out.println("MeshSensor shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return false;
    }
}
