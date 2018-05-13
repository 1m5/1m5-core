package io.onemfive.core.sensors;

import io.onemfive.core.sensors.i2p.bote.I2PBoteSensor;
import io.onemfive.core.sensors.tor.TorSensor;
import io.onemfive.core.util.AppThread;
import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.sensors.i2p.I2PSensor;
import io.onemfive.core.sensors.mesh.MeshSensor;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

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
    public void handleDocument(Envelope envelope) {
        Route r = (Route)envelope.getHeader(Envelope.ROUTE);
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        if(r.getOperation().startsWith("http") || r.getOperation().endsWith(".onion") && activeSensors.containsKey(TorSensor.class.getName())) {
            // Use Tor
            System.out.println(SensorsService.class.getName()+": using Tor Sensor...");
        } else if(r.getOperation().endsWith(".i2p") && activeSensors.containsKey(I2PSensor.class.getName())) {
            // Use I2P
            System.out.println(SensorsService.class.getName()+": using I2P Sensor...");
        } else if(r.getOperation().endsWith(".bote") && activeSensors.containsKey(I2PBoteSensor.class.getName())) {
            // Use I2P Bote
            System.out.println(SensorsService.class.getName()+": using I2P Bote Sensor...");
            I2PBoteSensor i2PBoteSensor = (I2PBoteSensor)activeSensors.get(I2PBoteSensor.class.getName());

        }
    }

    @Override
    public void handleEvent(Envelope envelope) {
        super.handleEvent(envelope);

    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(SensorsService.class.getSimpleName()+": starting...");
        try {
            config = Config.loadFromClasspath("sensors.config", properties);

            String registeredSensorsString = config.getProperty("1m5.sensors.registered");
            if(registeredSensorsString != null) {
                List<String> registered = Arrays.asList(registeredSensorsString.split(","));

                registeredSensors = new HashMap<>(registered.size());
                activeSensors = new HashMap<>(registered.size());

                if (registered.contains("bote")) {
                    registeredSensors.put(I2PBoteSensor.class.getName(), new I2PBoteSensor());
                    new AppThread(new Runnable() {
                        @Override
                        public void run() {
                            I2PBoteSensor i2PBoteSensor = (I2PBoteSensor) registeredSensors.get(I2PBoteSensor.class.getName());
                            i2PBoteSensor.start(config);
                            activeSensors.put(I2PBoteSensor.class.getName(), i2PBoteSensor);
                        }
                    }, SensorsService.class.getSimpleName()+":I2PBoteSensorStartThread").start();
                }

                if (registered.contains("i2p")) {
                    registeredSensors.put(I2PSensor.class.getName(), new I2PSensor());
                    new AppThread(new Runnable() {
                        @Override
                        public void run() {
                            I2PSensor i2PSensor = (I2PSensor) registeredSensors.get(I2PSensor.class.getName());
                            i2PSensor.start(config);
                            activeSensors.put(I2PSensor.class.getName(), i2PSensor);
                        }
                    }, SensorsService.class.getSimpleName()+":I2PSensorStartThread").start();
                }

                if (registered.contains("tor")) {
                    registeredSensors.put(TorSensor.class.getName(), new TorSensor());
                    new AppThread(new Runnable() {
                        @Override
                        public void run() {
                            TorSensor torSensor = (TorSensor) registeredSensors.get(TorSensor.class.getName());
                            torSensor.start(config);
                            activeSensors.put(TorSensor.class.getName(), torSensor);
                        }
                    }, SensorsService.class.getSimpleName()+":TorSensorStartThread").start();
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
                    }, SensorsService.class.getSimpleName()+":MeshSensorStartThread").start();
                }
            }

            System.out.println(SensorsService.class.getSimpleName()+": started.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(SensorsService.class.getSimpleName()+": failed to start.");
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
