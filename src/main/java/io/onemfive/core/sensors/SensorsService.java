package io.onemfive.core.sensors;

import io.onemfive.core.*;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;

import java.util.*;
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
    public static final String OPERATION_REPLY = "REPLY";

    private Properties config;

    private SensorManager sensorManager;

    public SensorsService(MessageProducer producer, ServiceStatusListener serviceStatusListener, SensorManager sensorManager) {
        super(producer, serviceStatusListener);
        this.sensorManager = sensorManager;
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

    @Override
    public void handleCommand(Envelope e) {
        SensorsServiceCommand c = (SensorsServiceCommand)DLC.getData(SensorsServiceCommand.class,e);
        if(c == null) {
            LOG.warning("No SensorsServiceCommand found in Envelope data while sending to SensorsService.");
            c = new SensorsServiceCommand();
            c.errorCode = SensorsServiceCommand.REQUEST_REQUIRED;
            DLC.addData(SensorsServiceCommand.class,c,e);
            return;
        }
        if(c.sensorManagerImplementation != null) {
            try {
                sensorManager = (SensorManager) Class.forName(c.sensorManagerImplementation).newInstance();
            } catch (Exception e1) {
                c.exception = e1;
                return;
            }
        }
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
        Sensor sensor = sensorManager.selectSensor(e);
        if(sensor != null) {
            switch (r.getOperation()) {
                case OPERATION_SEND : {
                    LOG.fine("Sending Envelope to selected Sensor...");
                    sensor.send(e);
                }
                case OPERATION_REPLY : {
                    LOG.fine("Replying with Envelope to requester...");
                    sensor.reply(e);
                }
                default: {
                    LOG.warning("Operation not supported. Sending to Dead Letter queue.");
                    deadLetter(e);
                }
            }
        } else {
            LOG.warning("Unable to determine sensor. Sending to Dead Letter queue.");
            deadLetter(e);
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
    void updateSensorStatus(final String sensorID, SensorStatus sensorStatus) {
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
                sensorManager.sensorError(sensorID);
                break;
            }
            default: LOG.warning("Sensor Status not being handled: "+sensorStatus.name());
        }
    }

    private Boolean allSensorsWithStatus(SensorStatus sensorStatus) {
        Collection<Sensor> sensors = ((SensorManagerBase)sensorManager).getActiveSensors().values();
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
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);
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
        sensorManager.shutdown();
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        // TODO: add wait/checks to ensure each sensor shutdowns
        return shutdown();
    }

    public void setManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

}
