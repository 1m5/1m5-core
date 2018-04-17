package io.onemfive.core.sensors;

import io.onemfive.core.bus.AppThread;
import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.Config;
import io.onemfive.core.bus.MessageProducer;
import io.onemfive.core.sensors.i2p.I2PSensor;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This is the main entry point into the application by supported networks.
 * It registers all supported/configured Sensors and manages their lifecycle.
 * Each supported Sensor provides a Message Queue each for:
 *  inbound (from Sensor)
 *  outbound (to Sensor)
 */
public class SensorsService extends BaseService {

    private Properties config;
    private List<String> activeSensors;

    private I2PSensor i2PSensor;

    public SensorsService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("SensorsService starting...");
        try {
            config = Config.load("sensors.config", properties);

            String activeSensorsString = config.getProperty("sc.sensors.active");
            activeSensors = Arrays.asList(activeSensorsString.split(","));

            if(activeSensors.contains("i2p")) {
                i2PSensor = new I2PSensor();
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        i2PSensor.start(config);
                    }
                }, "SensorsService:I2PSensorStartThread").start();
            }

            System.out.println("SensorsService started.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SensorsService failed to start.");
            return false;
        }
        return true;
    }


    @Override
    public boolean gracefulShutdown() {
        if(activeSensors.contains("i2p")) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    i2PSensor.gracefulShutdown();
                }
            }).start();
        }
        return true;
    }
}
