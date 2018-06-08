package io.onemfive.core.sensors;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class BaseSensor implements Sensor {

    protected SensorsService sensorsService;

    public BaseSensor(SensorsService sensorsService) {
        this.sensorsService = sensorsService;
    }
}
