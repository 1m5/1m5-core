package io.onemfive.core.sensors;

import io.onemfive.core.util.AppThread;
import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.sensors.i2p.I2PSensor;
import io.onemfive.core.sensors.mesh.MeshSensor;

import java.util.*;

/**
 * This is the main entry point into the application by supported networks.
 * It registers all supported/configured Sensors and manages their lifecycle.
 *
 *  @author ObjectOrange
 */
public class SensorsService extends BaseService {

    private Properties config;
    private Map<String, Sensor> registeredSensors;
    private Map<String, Sensor> activeSensors;

    public SensorsService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("SensorsService starting...");
        try {
            config = Config.load("sensors.config", properties);

            String registeredSensorsString = config.getProperty("1m5.sensors.registered");
            if(registeredSensorsString != null) {
                List<String> registered = Arrays.asList(registeredSensorsString.split(","));

                registeredSensors = new HashMap<>(registered.size());
                activeSensors = new HashMap<>(registered.size());

                if (registered.contains("i2p")) {
                    registeredSensors.put(I2PSensor.class.getName(), new I2PSensor());
                    new AppThread(new Runnable() {
                        @Override
                        public void run() {
                            I2PSensor i2PSensor = (I2PSensor) registeredSensors.get(I2PSensor.class.getName());
                            i2PSensor.start(config);
                            activeSensors.put(I2PSensor.class.getName(), i2PSensor);
                        }
                    }, "SensorsService:I2PSensorStartThread").start();
                }

                if (registered.contains("mesh")) {
                    registeredSensors.put(MeshSensor.class.getName(), new MeshSensor());
                    new AppThread(new Runnable() {
                        @Override
                        public void run() {
                            MeshSensor meshSensor = (MeshSensor) registeredSensors.get(MeshSensor.class.getName());
                            meshSensor.start(config);
                            activeSensors.put(MeshSensor.class.getName(), meshSensor);
                        }
                    }, "SensorsService:MeshSensorStartThread").start();
                }
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
        if(registeredSensors.containsKey(I2PSensor.class.getName())) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    I2PSensor i2PSensor = (I2PSensor)activeSensors.get(I2PSensor.class.getName());
                    i2PSensor.gracefulShutdown();
                }
            }).start();
        }
        if(registeredSensors.containsKey(MeshSensor.class.getName())) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    MeshSensor meshSensor = (MeshSensor)activeSensors.get(MeshSensor.class.getName());
                    meshSensor.shutdown();
                }
            }).start();
        }
        return true;
    }
}
