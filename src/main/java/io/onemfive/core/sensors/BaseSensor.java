package io.onemfive.core.sensors;

/**
 * A Base for common data and operations across all Sensors to provide a basic framework for them.
 *
 * @author objectorange
 */
public abstract class BaseSensor implements Sensor {

    protected SensorsService sensorsService;
    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    protected Integer restartAttempts = 0;

    protected void updateStatus(SensorStatus sensorStatus) {
        this.sensorStatus = sensorStatus;
        if(sensorsService != null) // Might be null during localized testing
            sensorsService.updateSensorStatus(getSensorID(), sensorStatus);
    }

    public BaseSensor(SensorsService sensorsService) {
        this.sensorsService = sensorsService;
    }

    protected abstract SensorID getSensorID();

    public SensorStatus getStatus() {
        return sensorStatus;
    }

    @Override
    public Integer getRestartAttempts() {
        return restartAttempts;
    }
}
