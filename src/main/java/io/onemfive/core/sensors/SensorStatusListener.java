package io.onemfive.core.sensors;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface SensorStatusListener {
    void sensorStatusChanged(SensorID sensorID, SensorStatus sensorStatus);
}
