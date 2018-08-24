package io.onemfive.core.sensors;

import io.onemfive.core.*;
import io.onemfive.core.sensors.clearnet.ClearnetSensor;
import io.onemfive.core.sensors.i2p.bote.I2PBoteSensor;
import io.onemfive.core.sensors.tor.TorSensor;
import io.onemfive.core.util.AppThread;
import io.onemfive.core.sensors.i2p.I2PSensor;
import io.onemfive.core.sensors.mesh.MeshSensor;
import io.onemfive.core.util.SystemVersion;
import io.onemfive.core.util.Wait;
import io.onemfive.data.Envelope;
import io.onemfive.data.Peer;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main entry point into the application by supported networks.
 * It registers all supported/configured Sensors and manages their lifecycle.
 * All Sensors' status has an effect on the SensorsService status which is
 * monitored by the ServiceBus.
 *
 * Sensitivity order from least to greatest is defined in Envelope.Sensitivity with default protocols:
 *
 * NONE: HTTP
 * LOW: HTTPS
 * MEDIUM: Tor
 * HIGH: I2P
 * VERYHIGH: I2P Bote
 * EXTREME: Mesh
 * NEO: A combination of all anonymous networks from MEDIUM to EXTREME
 *
 * We are working towards providing the following sensitivity routing logic:
 *
 * ** 1M5 Inter-Node Communications **
 * All communications between 1M5 peers defaults to I2P unless the Envelope's sensitivity
 * is set to VERYHIGH which indicates that higher sensitivity is required with
 * acceptable higher latency or when set to EXTREME and MESH is available.
 * 1M5's communication foundation starts with I2P as it provides the lowest latency / greatest
 * privacy trade-off. MESH is not yet available so a Sensitivity of EXTREME will use
 * I2P Bote with random delays set high.
 *
 * ** EXTERNAL COMMUNICATIONS **
 * All communications specifying an external HTTP URL will:
 *
 * * NONE *
 * Use HTTPS if specified in the URL otherwise HTTP. If HTTPS fails, fall back to HTTP.
 *
 * * LOW *
 * If HTTP supplied in URL, try HTTPS anyways.
 *
 * * MEDIUM *
 * Use Tor to reach specified HTTP/HTTPS URL.
 *
 * * HIGH *
 * If HTTP/HTTPS URL specified, use peers through I2P to reach a peer that can successfully use Tor.
 *
 * * VERYHIGH *
 * If HTTP/HTTPS URL specified, use peers through I2P Bote to reach a peer that can successfully use Tor.
 *
 * * EXTREME *
 * If HTTP/HTTPS URL specified and MESH not available, use peers through I2P Bote with high random delays
 * to reach a peer that can successfully use Tor. If MESH is available, use that instead.
 *
 * * GENERAL PEER PROPAGATION *
 * 1. If any of the above fails, send request to another peer via I2P to have it attempt it.
 * 2. If the protocol specified fails at the peer, it will forward onto randomly chosen
 * (likely to get smarter in future) next peer and retry.
 * 3. This will occur for specified number of attempts up to a maximum 10 until tokenization is implemented
 * at which it will continue until supplied tokens for transaction are exhausted.
 * 4. If I2P fails during any of these attempts and MESH is available, MESH will take over.
 *
 * This logic is/will be implemented in a Sensor Manager.
 *
 * The SensorManagerSimple class is a very basic implementation.
 *
 * The SensorManagerNeo4j is more complex using the Neo4j Graph database embedded.
 * The 1M5 Neo4j library must be included to use this.
 *
 *  @author objectorange
 */
public class SensorsService extends BaseService {

    private static final Logger LOG = Logger.getLogger(SensorsService.class.getName());

    public static final String OPERATION_SEND = "SEND";
    public static final String OPERATION_REPLY_CLEARNET = "REPLY_CLEARNET";

    private Properties config;
    private Map<SensorID, Sensor> registeredSensors;
    private Map<SensorID, Sensor> activeSensors;
    private SensorManager sensorManager;

    public SensorsService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        handleAll(envelope);
    }

    @Override
    public void handleEvent(Envelope envelope) {
        handleAll(envelope);
    }

    @Override
    public void handleHeaders(Envelope envelope) {
        handleAll(envelope);
    }

    private void handleAll(Envelope e) {
        SensorRequest request = (SensorRequest)DLC.getData(SensorRequest.class,e);
        if(request == null) {
            LOG.warning("No SensorRequest found in Envelope data while sending to SensorsService.");
            return;
        }
        if(request.from == null) {
            LOG.warning("No fromDID found in SensorRequest while sending to SensorsService.");
            return;
        }
        if(request.to == null) {
            LOG.warning("No toDID found in SensorRequest while sending to SensorsService.");
            return;
        }
        Route r = e.getRoute();
        Sensor sensor = null;
        if(Envelope.Sensitivity.MEDIUM.equals(e.getSensitivity())
                || r.getOperation().endsWith(".onion")
                || (e.getURL() != null && e.getURL().getProtocol() != null && e.getURL().getProtocol().endsWith(".onion"))
                && activeSensors.containsKey(SensorID.TOR)) {
            // Use Tor
            LOG.fine("Using Tor Sensor...");

            sensor = activeSensors.get(SensorID.TOR);
        } else if(Envelope.Sensitivity.HIGH.equals(e.getSensitivity())
                || r.getOperation().endsWith(".i2p")
                || (e.getURL() != null && e.getURL().getProtocol() != null && e.getURL().getProtocol().endsWith(".i2p"))
                && activeSensors.containsKey(SensorID.I2P)) {
            // Use I2P
            LOG.fine("Using I2P Sensor...");
            sensor = activeSensors.get(SensorID.I2P);
        } else if(Envelope.Sensitivity.VERYHIGH.equals(e.getSensitivity())
                || r.getOperation().endsWith(".bote")
                || (e.getURL() != null && e.getURL().getProtocol() != null && e.getURL().getProtocol().endsWith(".bote"))
                && activeSensors.containsKey(SensorID.I2PBOTE)) {
            // Use I2P Bote
            LOG.fine("Using I2P Bote Sensor...");
            long maxWaitMs = 30 * 1000;
            long waitTimeMs = 3 * 1000;
            long currentWaitMs = 0L;
            do {
                sensor = activeSensors.get(SensorID.I2PBOTE);
                if(sensor == null) {
                    Wait.aMs(waitTimeMs); // wait 3 seconds
                    currentWaitMs += waitTimeMs;
                }
            } while(sensor == null && currentWaitMs < maxWaitMs);
        } else if(Envelope.Sensitivity.EXTREME.equals(e.getSensitivity())
                || r.getOperation().endsWith(".mesh")
                || (e.getURL() != null && e.getURL().getProtocol() != null && e.getURL().getProtocol().endsWith(".mesh"))
                && activeSensors.containsKey(SensorID.MESH)) {
            // Use Mesh
            LOG.fine("Using Mesh Sensor...");
            sensor = activeSensors.get(SensorID.MESH);
        } else if(Envelope.Sensitivity.NONE.equals(e.getSensitivity())
                || Envelope.Sensitivity.LOW.equals(e.getSensitivity())
                || r.getOperation().startsWith("http")
                || e.getURL() != null && e.getURL().getProtocol() != null && e.getURL().getProtocol().startsWith("http")) {
            // Use Clearnet
            LOG.fine("Using Clearnet Sensor...");
            sensor = activeSensors.get(SensorID.CLEARNET);
        }
        if(sensor != null) {
            if(OPERATION_SEND.equals(r.getOperation())) {
                LOG.fine("Sending Envelope to selected Sensor...");
                sensor.send(e);
            }
        } else {
            if (r.getOperation().equals(OPERATION_REPLY_CLEARNET)) {
                sensor = activeSensors.get(SensorID.CLEARNET);
                sensor.reply(e);
            } else {
                LOG.warning("Unable to determine sensor. Sending to Dead Letter queue.");
                deadLetter(e);
            }
        }
    }

    public void sendToBus(Envelope envelope) {
        LOG.info("Sending request to service bus from Sensors Service...");
        int maxAttempts = 30;
        int attempts = 0;
        while(!producer.send(envelope) && ++attempts <= maxAttempts) {
            synchronized (this) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {}
            }
        }
        if(attempts == maxAttempts) {
            // failed
            DLC.addErrorMessage("500",envelope);
        }
    }

    /**
     * Based on supplied SensorStatus, set the SensorsService status.
     * @param sensorID
     * @param sensorStatus
     */
    void updateSensorStatus(final SensorID sensorID, SensorStatus sensorStatus) {
        ServiceStatus currentServiceStatus = getServiceStatus();
        LOG.info("Status updated to: "+sensorStatus.name());
        switch (sensorStatus) {
            case INITIALIZING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case STARTING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case WAITING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                else if(currentServiceStatus == ServiceStatus.STARTING)
                    updateStatus(ServiceStatus.WAITING);
                break;
            }
            case NETWORK_WARMUP: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case NETWORK_CONNECTING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case NETWORK_CONNECTED: {
                if(allSensorsWithStatus(SensorStatus.NETWORK_CONNECTED)) {
                    LOG.info("All Sensors Connected to their networks, updating SensorService status to RUNNING.");
                    updateStatus(ServiceStatus.RUNNING);
                }
                break;
            }
            case NETWORK_STOPPING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case NETWORK_STOPPED: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case NETWORK_ERROR: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case PAUSING: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case PAUSED: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case UNPAUSING: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case SHUTTING_DOWN: {
                break; // Not handling
            }
            case GRACEFULLY_SHUTTING_DOWN: {
                break; // Not handling
            }
            case SHUTDOWN: {
                if(allSensorsWithStatus(SensorStatus.SHUTDOWN)) {
                    if(getServiceStatus() != ServiceStatus.RESTARTING) {
                        updateStatus(ServiceStatus.SHUTDOWN);
                    }
                }
                break;
            }
            case GRACEFULLY_SHUTDOWN: {
                if(allSensorsWithStatus(SensorStatus.GRACEFULLY_SHUTDOWN)) {
                    if(getServiceStatus() == ServiceStatus.RESTARTING) {
                        start(this.config);
                    } else {
                        updateStatus(ServiceStatus.GRACEFULLY_SHUTDOWN);
                    }
                }
                break;
            }
            case RESTARTING: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case ERROR: {
                if(allSensorsWithStatus(SensorStatus.ERROR)) {
                    // Major issues - all sensors error - flag for restart of Service
                    updateStatus(ServiceStatus.UNSTABLE);
                    break;
                }
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                // Sensor has Error, restart it if number of restarts is not greater than 3
                if(activeSensors.get(sensorID) != null) {
                    if(activeSensors.get(sensorID).getRestartAttempts() <= 3) {
                        new AppThread(new Runnable() {
                            @Override
                            public void run() {
                                activeSensors.get(sensorID).restart();
                            }
                        }).start();
                    } else {
                        // Sensor is apparently not working. Unregister it.
                        activeSensors.remove(sensorID);
                    }
                }
                break;
            }
            default: LOG.warning("Sensor Status not being handled: "+sensorStatus.name());
        }
    }

    private Boolean allSensorsWithStatus(SensorStatus sensorStatus) {
        Collection<Sensor> sensors = activeSensors.values();
        for(Sensor s : sensors) {
            if(s.getStatus() != sensorStatus){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.setLevel(Level.INFO);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);
        try {
            config = Config.loadFromClasspath("sensors.config", properties, false);
            if (SystemVersion.isAndroid()) {
                sensorManager = new SensorManagerSimple();
                LOG.info("System is Android, using SensorManagerSimple.");
            } else {
                sensorManager = (SensorManager) Class.forName("io.onemfive.neo4j.SensorManagerNeo4j").newInstance();
                LOG.info("System is not Android, using SensorManagerNeo4j.");
            }
            // TODO: Test loadPeers
//            loadPeers();
            registerSensors();

            LOG.info("Started.");
        } catch (ClassNotFoundException e) {
            LOG.warning(e.getLocalizedMessage());
            LOG.warning("SensorManager implementation class not found so defaulting to SensorManagerSimple");
            sensorManager = new SensorManagerSimple();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warning("Failed to start.");
            return false;
        }
        sensorManager.init(properties);
        return true;
    }

    @Override
    public boolean restart() {
        updateStatus(ServiceStatus.RESTARTING);
        gracefulShutdown();
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        if(getServiceStatus() != ServiceStatus.RESTARTING)
            updateStatus(ServiceStatus.SHUTTING_DOWN);
        if(registeredSensors.containsKey(SensorID.CLEARNET)) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    Sensor s = activeSensors.get(SensorID.CLEARNET);
                    if(s != null) s.gracefulShutdown();
                }
            }).start();
        }
        if(registeredSensors.containsKey(SensorID.MESH)) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    Sensor s = activeSensors.get(SensorID.MESH);
                    if(s != null) s.gracefulShutdown();
                }
            }).start();
        }
        if(registeredSensors.containsKey(SensorID.TOR)) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    Sensor s = activeSensors.get(SensorID.TOR);
                    if(s != null) s.gracefulShutdown();
                }
            }).start();
        }
        if(registeredSensors.containsKey(SensorID.I2P)) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    Sensor s = activeSensors.get(SensorID.I2P);
                    if(s != null) s.gracefulShutdown();
                }
            }).start();
        }
        if(registeredSensors.containsKey(SensorID.I2PBOTE)) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    Sensor s = activeSensors.get(SensorID.I2PBOTE);
                    if(s != null) s.gracefulShutdown();
                }
            }).start();
        }
        sensorManager.shutdown();
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        // TODO: add wait/checks to ensure each sensor shutdowns
        return shutdown();
    }

    private void registerSensors() {
        String registeredSensorsString = config.getProperty("1m5.sensors.registered");
        if(registeredSensorsString != null) {
            List<String> registered = Arrays.asList(registeredSensorsString.split(","));

            registeredSensors = new HashMap<>(registered.size());
            activeSensors = new HashMap<>(registered.size());

            if (registered.contains("i2p")) {
                registeredSensors.put(SensorID.I2P, new I2PSensor(this));
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        I2PSensor i2PSensor = (I2PSensor) registeredSensors.get(SensorID.I2P);
                        i2PSensor.start(config);
                        activeSensors.put(SensorID.I2P, i2PSensor);
                        LOG.info("I2PSensor registered as active.");
                    }
                }, SensorsService.class.getSimpleName()+":I2PSensorStartThread").start();
            }

            if (registered.contains("bote")) {
                registeredSensors.put(SensorID.I2PBOTE, new I2PBoteSensor(this));
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        I2PBoteSensor i2PBoteSensor = (I2PBoteSensor) registeredSensors.get(SensorID.I2PBOTE);
                        i2PBoteSensor.start(config);
                        activeSensors.put(SensorID.I2PBOTE, i2PBoteSensor);
                        LOG.info("I2PBoteSensor registered as active.");
                    }
                }, SensorsService.class.getSimpleName()+":I2PBoteSensorStartThread").start();
            }

            if (registered.contains("tor")) {
                registeredSensors.put(SensorID.TOR, new TorSensor(this));
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        TorSensor torSensor = (TorSensor) registeredSensors.get(SensorID.TOR);
                        torSensor.start(config);
                        activeSensors.put(SensorID.TOR, torSensor);
                        LOG.info("TorSensor registered as active.");
                    }
                }, SensorsService.class.getSimpleName()+":TorSensorStartThread").start();
            }

            if (registered.contains("mesh")) {
                registeredSensors.put(SensorID.MESH, new MeshSensor(this));
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        MeshSensor meshSensor = (MeshSensor) registeredSensors.get(SensorID.MESH);
                        meshSensor.start(config);
                        activeSensors.put(SensorID.TOR, meshSensor);
                        LOG.info("MeshSensor registered as active.");
                    }
                }, SensorsService.class.getSimpleName()+":MeshSensorStartThread").start();
            }

            if (registered.contains("clearnet")) {
                registeredSensors.put(SensorID.CLEARNET, new ClearnetSensor(this));
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        ClearnetSensor clearnetSensor = (ClearnetSensor) registeredSensors.get(SensorID.CLEARNET);
                        clearnetSensor.start(config);
                        activeSensors.put(SensorID.CLEARNET, clearnetSensor);
                        LOG.info("ClearnetSensor registered as active.");
                    }
                }, SensorsService.class.getSimpleName()+":ClearnetSensorStartThread").start();
            }
        }
    }

}
