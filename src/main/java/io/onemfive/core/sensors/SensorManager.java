package io.onemfive.core.sensors;

import io.onemfive.data.Envelope;
import io.onemfive.data.Peer;

import java.util.Map;
import java.util.Properties;

public interface SensorManager {
    boolean init(Properties properties);
    Sensor selectSensor(Envelope envelope);
    void registerSensor(Sensor sensor);
    void updatePeer(Peer peer);
    Map<String,Peer> getAllPeers();
    void sensorError(String sensorClass);
    boolean shutdown();
}
