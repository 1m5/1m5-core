package io.onemfive.core.bus;

import io.onemfive.core.*;
import io.onemfive.core.admin.AdminService;
import io.onemfive.core.did.DIDService;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.ipfs.IPFSService;
import io.onemfive.core.keyring.KeyRingService;
import io.onemfive.core.prana.PranaService;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.repository.RepositoryService;
import io.onemfive.core.orchestration.OrchestrationService;
import io.onemfive.core.infovault.InfoVault;
import io.onemfive.core.securedrop.SecureDropService;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.core.util.AppThread;
import io.onemfive.data.Envelope;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Encompasses all functionality needed to support messaging between
 * all internal services and their life cycles.
 *
 * Provides a Staged Event-Driven Architecture (SEDA) by providing
 * channels to/from all Services.
 *
 * All bus threads come from one pool to help manage resource usage.
 *
 * TODO: Handles high priority commands to manage the services synchronously.
 * TODO: Add configurations
 *
 * @author objectorange
 */
public final class ServiceBus implements MessageProducer, LifeCycle, ServiceRegistrar {

    private static final Logger LOG = Logger.getLogger(ServiceBus.class.getName());

    public enum Status {Starting, Running, Stopping, Stopped}

    private Status status = Status.Stopped;

    private Properties properties;

    private volatile WorkerThreadPool pool;
    private MessageChannel channel;

    private ClientAppManager clientAppManager;
    private Map<String, BaseService> registeredServices;
    private Map<String, BaseService> runningServices;

    // TODO: Set maxThreads by end-user max processing allocation (Prana limitations)
    private int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
    // TODO: Set maxMessagesCached by end-user max memory allocation (Prana limitations)
    private int maxMessagesCached = 10 * maxThreads;

    private final AtomicBoolean spin = new AtomicBoolean(true);

    public ServiceBus(Properties properties, ClientAppManager clientAppManager) {
        this.properties = properties;
        this.clientAppManager = clientAppManager;
        LOG.info("Instantiated with maxThreads="+maxThreads+" and maxMessagesCached="+maxMessagesCached);
    }

    @Override
    public boolean send(Envelope envelope) {
        LOG.info("Received envelope. Sending to channel...");
        if(pool != null && pool.getStatus() == WorkerThreadPool.Status.Running) {
            return channel.send(envelope);
        } else {
            LOG.warning("Unable to send to channel: pool.status="+pool.getStatus().toString());
            return false;
        }
    }

    public void register(Class serviceClass) throws ServiceNotAccessibleException, ServiceNotSupportedException, ServiceRegisteredException {
        LOG.info("Registering service class: "+serviceClass.getName());
        if(registeredServices.containsKey(serviceClass.getName())) {
            throw new ServiceRegisteredException();
        }
        final String serviceName = serviceClass.getName();
        try {
            final BaseService service = (BaseService)serviceClass.newInstance();
            service.setProducer(this);
            // register service
            registeredServices.put(serviceClass.getName(), service);
            LOG.info("Service registered successfully: "+serviceName);
            // start registered service
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    if(service.start(properties)) {
                        runningServices.put(serviceName, service);
                        LOG.info("Service registered successfully as running: "+serviceName);
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
                        LOG.info("Service unregistered successfully: "+serviceName);
                    }
                }
            }, serviceName+"-ShutdownThread").start();
        }
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
        status = Status.Starting;
        boolean startupSuccessful = true;

        if(properties != null) {
            if(this.properties == null)
                this.properties = properties;
            else
                this.properties.putAll(properties);
        }

        try {
            this.properties = Config.loadFromClasspath("bus.config", this.properties);
            String maxMessagesCachedMultiplierStr = this.properties.getProperty("1m5.bus.maxMessagesCachedMultiplier");
            if(maxMessagesCachedMultiplierStr != null){
                maxMessagesCached = Integer.parseInt(maxMessagesCachedMultiplierStr) * maxThreads;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warning("Failed to load bus.config in ServiceBus.");
        }

        channel = new MessageChannel(maxMessagesCached);

        registeredServices = new HashMap<>(13);
        runningServices = new HashMap<>(13);

        // Register Core Services
        AdminService adminService = new AdminService(this);
        registeredServices.put(AdminService.class.getName(), adminService);

        // Start InfoVaultService first synchronously to ensure InfoVault is started prior to other services getting an instance
        InfoVaultService infoVaultService = new InfoVaultService(this);
        registeredServices.put(InfoVaultService.class.getName(), infoVaultService);

        OrchestrationService orchestrationService = new OrchestrationService(this);
        registeredServices.put(OrchestrationService.class.getName(), orchestrationService);

        DIDService didService = new DIDService(this);
        registeredServices.put(DIDService.class.getName(), didService);

        IPFSService ipfsService = new IPFSService(this);
        registeredServices.put(IPFSService.class.getName(), ipfsService);

//        PranaService pranaService = new PranaService(this);
//        registeredServices.put(PranaService.class.getName(), pranaService);

//        ConsensusService consensusService = new ConsensusService(this);
//        registeredServices.put(ConsensusService.class.getName(), consensusService);

//        ContentService contentService = new ContentService(this);
//        registeredServices.put(ContentService.class.getName(), contentService);

//        DEXService dexService = new DEXService(this);
//        registeredServices.put(DEXService.class.getName(), dexService);

//        RepositoryService repositoryService = new RepositoryService(this);
//        registeredServices.put(RepositoryService.class.getName(), repositoryService);

//        KeyRingService keyRingService = new KeyRingService(this);
//        registeredServices.put(KeyRingService.class.getName(), keyRingService);

//        PaymentService paymentService = new PaymentService(this);
//        registeredServices.put(PaymentService.class.getName(), paymentService);

        SensorsService sensorsService = new SensorsService(this);
        registeredServices.put(SensorsService.class.getName(), sensorsService);

//        AtenService atenService = new AtenService(this);
//        registeredServices.put(AtenService.class.getName(), atenService);

//        SecureDropService secureDropService = new SecureDropService(this);
//        registeredServices.put(SecureDropService.class.getName(), secureDropService);

        // Start Registered Services
        final Properties props = this.properties;
        for(final String serviceName : registeredServices.keySet()) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    BaseService service = registeredServices.get(serviceName);
                    if(service.start(props)) {
                        runningServices.put(serviceName, service);
                    }
                }
            }, serviceName+"-StartupThread").start();
        }

        pool = new WorkerThreadPool(clientAppManager, runningServices, channel, maxThreads, maxThreads, properties);
        pool.start();

        status = Status.Running;
        return startupSuccessful;
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
        LOG.info("Shutting down...");
        status = Status.Stopping;
        spin.set(false);
        pool.shutdown();
        channel.shutdown();
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
        status = Status.Stopped;
        LOG.info("Shutdown.");
        return true;
    }

    /**
     * Ensure shutdown is graceful
     *
     * TODO: Implement
     *
     * @return
     */
    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

    public Status getStatus() {
        return status;
    }
}
