package io.onemfive.core.bus;

import io.onemfive.core.*;
import io.onemfive.core.admin.AdminService;
import io.onemfive.core.did.DIDService;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.keyring.KeyRingService;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.orchestration.OrchestrationService;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.core.util.AppThread;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Encompasses all functionality needed to support messaging between
 * all internal services and their life cycles.
 *
 * Provides a Staged Event-Driven Architecture (SEDA) by providing a
 * channel to/from all Services.
 *
 * All bus threads come from one pool to help manage resource usage.
 *
 * TODO: Handles high priority commands to manage the services synchronously.
 * TODO: Add configurations
 *
 * @author objectorange
 */
public final class ServiceBus implements MessageProducer, LifeCycle, ServiceRegistrar, ServiceStatusListener {

    private static final Logger LOG = Logger.getLogger(ServiceBus.class.getName());

    public enum Status {Starting, Running, Stopping, Stopped}

    private Status status = Status.Stopped;

    private Properties properties;

    private volatile WorkerThreadPool pool;
    private MessageChannel channel;

    private ClientAppManager clientAppManager;
    private Map<String, BaseService> registeredServices;
    private Map<String, BaseService> runningServices;

    private List<BusStatusListener> busStatusListeners = new ArrayList<>();

    // TODO: Set maxThreads by end-user max processing allocation (Prana limitations)
    private int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
    // TODO: Set maxMessagesCached by end-user max memory allocation (Prana limitations)
    private int maxMessagesCached = 10 * maxThreads;

    private final AtomicBoolean spin = new AtomicBoolean(true);

    public ServiceBus(Properties properties, ClientAppManager clientAppManager) {
        this.properties = properties;
        this.clientAppManager = clientAppManager;
        LOG.finer("Instantiated with maxThreads="+maxThreads+" and maxMessagesCached="+maxMessagesCached);
    }

    @Override
    public boolean send(Envelope e) {
        LOG.finest("Received envelope. Sending to channel...");
        if(pool != null && pool.getStatus() == WorkerThreadPool.Status.Running) {
            return channel.send(e);
        } else {
            String errMsg = "Unable to send to channel: pool.status="+pool.getStatus().toString();
            DLC.addErrorMessage(errMsg, e);
            LOG.warning(errMsg);
            return false;
        }
    }

    public void registerBusStatusListener (BusStatusListener busStatusListener) {
        busStatusListeners.add(busStatusListener);
    }

    public void unregisterBusStatusListener(BusStatusListener busStatusListener) {
        busStatusListeners.remove(busStatusListener);
    }

    public void register(Class serviceClass, Properties p) throws ServiceNotAccessibleException, ServiceNotSupportedException, ServiceRegisteredException {
        LOG.finer("Registering service class: "+serviceClass.getName());
        if(registeredServices.containsKey(serviceClass.getName())) {
            throw new ServiceRegisteredException();
        }
        if(p != null && p.size() > 0)
            properties.putAll(p);
        final String serviceName = serviceClass.getName();
        try {
            final BaseService service = (BaseService)serviceClass.newInstance();
            service.setProducer(this);
            // register service
            registeredServices.put(serviceClass.getName(), service);
            service.registerServiceStatusListener(this);
            LOG.finer("Service registered successfully: "+serviceName);
            // init registered service
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    if(service.start(properties)) {
                        runningServices.put(serviceName, service);
                        LOG.finer("Service registered successfully as running: "+serviceName);
                    }
                }
            }, serviceName+"-StartupThread").start();
        } catch (InstantiationException e) {
            throw new ServiceNotSupportedException(e);
        } catch (IllegalAccessException e) {
            throw new ServiceNotAccessibleException(e);
        }
    }

    public void unregister(Class serviceClass) {
        if(runningServices.containsKey(serviceClass.getName())) {
            final String serviceName = serviceClass.getName();
            final BaseService service = runningServices.get(serviceName);
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    if(service.shutdown()) {
                        runningServices.remove(serviceName);
                        registeredServices.remove(serviceName);
                        LOG.finer("Service unregistered successfully: "+serviceName);
                    }
                }
            }, serviceName+"-ShutdownThread").start();
        }
    }

    private void updateStatus(Status status) {
        this.status = status;
        switch(status) {
            case Starting: {
                LOG.info("1M5 Service Bus is Starting");
                break;
            }
            case Running: {
                LOG.info("1M5 Service Bus is Running");
                break;
            }
            case Stopping: {
                LOG.info("1M5 Service Bus is Stopping");
                break;
            }
            case Stopped: {
                LOG.info("1M5 Service Bus has Stopped");
                break;
            }
        }
        LOG.info("Updating Bus Status Listeners; size="+busStatusListeners.size());
        for(BusStatusListener l : busStatusListeners) {
            l.busStatusChanged(status);
        }
    }

    @Override
    public void serviceStatusChanged(String serviceFullName, ServiceStatus serviceStatus) {
        LOG.info("Service ("+serviceFullName+") reporting new status("+serviceStatus.name()+") to Bus.");
        switch(serviceStatus) {
            case UNSTABLE: {
                // Service is Unstable - restart
                BaseService service = registeredServices.get(serviceFullName);
                if(service != null) {
                    LOG.warning("Service ("+serviceFullName+") reporting UNSTABLE; restarting...");
                    service.restart();
                }
                break;
            }
            case RUNNING: {
                if(allServicesWithStatus(ServiceStatus.RUNNING)) {
                    LOG.info("All Services are RUNNING therefore Bus updating status to RUNNING.");
                    updateStatus(Status.Running);
                }
                break;
            }
            case SHUTDOWN: {
                if(allServicesWithStatus(ServiceStatus.SHUTDOWN)) {
                    LOG.info("All Services are SHUTDOWN therefore Bus updating status to STOPPED.");
                    updateStatus(Status.Stopped);
                }
                break;
            }
            case GRACEFULLY_SHUTDOWN: {
                if(allServicesWithStatus(ServiceStatus.GRACEFULLY_SHUTDOWN)) {
                    LOG.info("All Services are GRACEFULLY_SHUTDOWN therefore Bus updating status to STOPPED.");
                    updateStatus(Status.Stopped);
                }
                break;
            }
        }
    }

    private Boolean allServicesWithStatus(ServiceStatus serviceStatus) {
        Collection<BaseService> services = registeredServices.values();
        for(BaseService s : services) {
            if(s.getServiceStatus() != serviceStatus){
                return false;
            }
        }
        return true;
    }

    /**
     * Starts up Service Bus registering internal services, starting all services registered, and starting message channel
     * and worker thread pool.
     *
     * TODO: Provide a method for externalizing service registration record for startup.
     *
     * @param properties
     * @return
     */
    @Override
    public boolean start(Properties properties) {
        updateStatus(Status.Starting);
        if(properties != null) {
            if(this.properties == null)
                this.properties = properties;
            else
                this.properties.putAll(properties);
        }

        try {
            this.properties = Config.loadFromClasspath("bus.config", this.properties, false);
            String maxMessagesCachedMultiplierStr = this.properties.getProperty("1m5.bus.maxMessagesCachedMultiplier");
            if(maxMessagesCachedMultiplierStr != null){
                maxMessagesCached = Integer.parseInt(maxMessagesCachedMultiplierStr) * maxThreads;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warning("Failed to load bus.config in ServiceBus.");
        }

        // TODO: should we init the pool before the channel?
        channel = new MessageChannel(maxMessagesCached);
        channel.start(properties);

        registeredServices = new HashMap<>(15);
        runningServices = new HashMap<>(15);

        final Properties props = this.properties;
        // Register Core Services - Place slowest to RUNNING services first
        InfoVaultService infoVaultService = new InfoVaultService(this, this);
        registeredServices.put(InfoVaultService.class.getName(), infoVaultService);
        // Start InfoVaultService first to ensure InfoVaultDB gets initialized before Services begin using it.
        infoVaultService.start(props);
        runningServices.put(InfoVaultService.class.getName(),infoVaultService);

        SensorsService sensorsService = new SensorsService(this, this);
        registeredServices.put(SensorsService.class.getName(), sensorsService);

        OrchestrationService orchestrationService = new OrchestrationService(this, this);
        registeredServices.put(OrchestrationService.class.getName(), orchestrationService);

        KeyRingService keyRingService = new KeyRingService(this, this);
        registeredServices.put(KeyRingService.class.getName(), keyRingService);

        DIDService didService = new DIDService(this, this);
        registeredServices.put(DIDService.class.getName(), didService);

        NotificationService notificationService = new NotificationService(this, this);
        registeredServices.put(NotificationService.class.getName(), notificationService);

        AdminService adminService = new AdminService(this, this);
        registeredServices.put(AdminService.class.getName(), adminService);

        // Additional Services should be registered by client via Admin Service

//        IPFSService ipfsService = new IPFSService(this, this);
//        registeredServices.put(IPFSService.class.getName(), ipfsService);

//        PranaService pranaService = new PranaService(this, this);
//        registeredServices.put(PranaService.class.getName(), pranaService);

//        ConsensusService consensusService = new ConsensusService(this, this);
//        registeredServices.put(ConsensusService.class.getName(), consensusService);

//        ContentService contentService = new ContentService(this, this);
//        registeredServices.put(ContentService.class.getName(), contentService);

//        DEXService dexService = new DEXService(this, this);
//        registeredServices.put(DEXService.class.getName(), dexService);

//        RepositoryService repositoryService = new RepositoryService(this, this);
//        registeredServices.put(RepositoryService.class.getName(), repositoryService);

//        CurrencyService paymentService = new CurrencyService(this, this);
//        registeredServices.put(CurrencyService.class.getName(), paymentService);

//        AtenService atenService = new AtenService(this, this);
//        registeredServices.put(AtenService.class.getName(), atenService);

//        SecureDropService secureDropService = new SecureDropService(this, this);
//        registeredServices.put(SecureDropService.class.getName(), secureDropService);

        // Start Registered Services
        for(final String serviceName : registeredServices.keySet()) {
            if(!serviceName.equals(InfoVaultService.class.getName())) {
                // InfoVaultService already started above
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        BaseService service = registeredServices.get(serviceName);
                        if (service.start(props)) {
                            runningServices.put(serviceName, service);
                        }
                    }
                }, serviceName + "-StartupThread").start();
            }
        }

        pool = new WorkerThreadPool(clientAppManager, runningServices, channel, maxThreads, maxThreads, properties);
        pool.start();

        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    /**
     * Shutdown the Service Bus
     *
     * TODO: Run in separate AppThread
     *
     * @return
     */
    @Override
    public boolean shutdown() {
        updateStatus(Status.Stopping);
        spin.set(false);
        pool.shutdown();
        channel.shutdown(); // TODO: Should we teardown channel before pool?
        for(final String serviceName : runningServices.keySet()) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    BaseService service = runningServices.get(serviceName);
                    if(service.shutdown()) {
                        runningServices.remove(serviceName);
                    }
                }
            }, serviceName+"-ShutdownThread").start();
        }
        return true;
    }

    /**
     * Ensure teardown is graceful by waiting until all Services indicate graceful teardown complete or timeout
     *
     * TODO: Implement
     *
     * @return
     */
    @Override
    public boolean gracefulShutdown() {
        updateStatus(Status.Stopping);
        spin.set(false);
        pool.shutdown();
        channel.shutdown(); // TODO: Should we teardown channel before pool?
        for(final String serviceName : runningServices.keySet()) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    BaseService service = runningServices.get(serviceName);
                    if(service.gracefulShutdown()) {
                        runningServices.remove(serviceName);
                    }
                }
            }, serviceName+"-GracefulShutdownThread").start();
        }
        return true;
    }

    public Status getStatus() {
        return status;
    }
}
