package io.onemfive.core.sensors;

import io.onemfive.data.Envelope;
import io.onemfive.data.Peer;

import java.util.HashMap;
import java.util.Map;

/**
 * A Base for common data and operations across all Sensors to provide a basic framework for them.
 *
 * @author objectorange
 */
public abstract class BaseSensor implements Sensor {

    protected SensorsService sensorsService;
    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    protected Integer restartAttempts = 0;
    private Envelope.Sensitivity sensitivity;
    private Integer priority;
    protected Map<String,Peer> peers = new HashMap<>();

    protected void updateStatus(SensorStatus sensorStatus) {
        this.sensorStatus = sensorStatus;
        if(sensorsService != null) // Might be null during localized testing
            sensorsService.updateSensorStatus(this.getClass().getName(), sensorStatus);
    }

    public BaseSensor(SensorsService sensorsService, Envelope.Sensitivity sensitivity, Integer priority) {
        this.sensorsService = sensorsService;
        this.sensitivity = sensitivity;
        this.priority = priority;
    }

    @Override
    public Map<String, Peer> getPeers() {
        return peers;
    }

    @Override
    public SensorStatus getStatus() {
        return sensorStatus;
    }

    @Override
    public Envelope.Sensitivity getSensitivity() {
        return sensitivity;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public Integer getRestartAttempts() {
        return restartAttempts;
    }
}
