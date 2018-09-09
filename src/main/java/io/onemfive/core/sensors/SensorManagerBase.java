package io.onemfive.core.sensors;

import io.onemfive.data.Peer;

import java.util.HashMap;
import java.util.Map;

public abstract class SensorManagerBase implements SensorManager {

    protected Map<String, Sensor> registeredSensors = new HashMap<>();
    protected Map<String, Sensor> activeSensors = new HashMap<>();
    protected Map<String, Sensor> blockedSensors = new HashMap<>();

    protected Map<String, Peer> peers = new HashMap<>();

    @Override
    public void registerSensor(Sensor sensor) {
        registeredSensors.put(sensor.getClass().getName(), sensor);
    }

    Map<String, Sensor> getRegisteredSensors() {
        return registeredSensors;
    }

    Map<String, Sensor> getActiveSensors() {
        return activeSensors;
    }

    Map<String, Sensor> getBlockedSensors(){
        return blockedSensors;
    }

    @Override
    public Map<String, Peer> getAllPeers() {
        return peers;
    }
}
