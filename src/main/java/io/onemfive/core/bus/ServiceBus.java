package io.onemfive.core.bus;

import io.onemfive.core.aten.AtenService;
import io.onemfive.core.prana.PranaService;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.consensus.ConsensusService;
import io.onemfive.core.content.ContentService;
import io.onemfive.core.dex.DEXService;
import io.onemfive.core.repository.RepositoryService;
import io.onemfive.core.orchestration.OrchestrationService;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.keyring.KeyRingService;
import io.onemfive.core.lid.LIDService;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.Envelope;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

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
 *
 * @author objectorange
 */
public class ServiceBus implements MessageProducer, LifeCycle {

    public enum Status {Starting, Running, Stopping, Stopped}

    private Status status = Status.Stopped;

    private Properties properties;

    private volatile WorkerThreadPool pool;
    private MessageChannel channel;

    private ClientAppManager clientAppManager;
    protected Map<String, BaseService> registeredServices;
    protected Map<String, BaseService> runningServices;

    // TODO: Set maxThreads by end-user max processing configuration
    private int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
    // TODO: Set maxMessagesCached by end-user max memory configuration
    private int maxMessagesCached = 10 * maxThreads;

    private final AtomicBoolean spin = new AtomicBoolean(true);

    public ServiceBus(Properties properties, ClientAppManager clientAppManager) {
        this.properties = properties;
        this.clientAppManager = clientAppManager;
    }

    @Override
    public boolean send(Envelope envelope) {
        System.out.println(ServiceBus.class.getSimpleName()+": Received reply. Sending to channel...");
        if(pool != null && pool.getStatus() == WorkerThreadPool.Status.Running) {
            return channel.send(envelope);
        } else {
            System.out.println(ServiceBus.class.getSimpleName()+": Unable to send to channel: pool.status="+pool.getStatus().toString());
            return false;
        }
    }

    public void register(Class serviceClass) throws InstantiationException, IllegalAccessException, ServiceRegisteredException {
        if(registeredServices.containsKey(serviceClass.getName())) {
            throw new ServiceRegisteredException();
        }
        BaseService service = (BaseService)serviceClass.newInstance();
        service.setProducer(this);
        registeredServices.put(serviceClass.getName(), service);
        if(service.start(properties)) {
            runningServices.put(serviceClass.getName(), service);
        }
    }

    public void unregister(Class serviceClass) {
        if(runningServices.containsKey(serviceClass.getName())) {
            BaseService service = runningServices.get(serviceClass.getName());
            if(service.shutdown()) {
                runningServices.remove(serviceClass.getName());
                registeredServices.remove(serviceClass.getName());
            }
        }
    }

    @Override
    public boolean start(Properties properties) {
        if(properties == null)
            properties = this.properties;
        status = Status.Starting;
        boolean startupSuccessful = true;

        channel = new MessageChannel( maxMessagesCached);

        registeredServices = new HashMap<>(13);
        runningServices = new HashMap<>(13);

        // Start Services
        // TODO: Start services using AppThreads to reduce startup time

        PranaService pranaService = new PranaService(this);
        registeredServices.put(PranaService.class.getName(), pranaService);

        ConsensusService consensusService = new ConsensusService(this);
        registeredServices.put(ConsensusService.class.getName(), consensusService);

        ContentService contentService = new ContentService(this);
        registeredServices.put(ContentService.class.getName(), contentService);

        DEXService dexService = new DEXService(this);
        registeredServices.put(DEXService.class.getName(), dexService);

        RepositoryService repositoryService = new RepositoryService(this);
        registeredServices.put(RepositoryService.class.getName(), repositoryService);

        InfoVaultService infoVaultService = new InfoVaultService(this);
        registeredServices.put(InfoVaultService.class.getName(), infoVaultService);

        KeyRingService keyRingService = new KeyRingService(this);
        registeredServices.put(KeyRingService.class.getName(), keyRingService);

        LIDService lidService = new LIDService(this);
        registeredServices.put(LIDService.class.getName(), lidService);

        OrchestrationService orchestrationService = new OrchestrationService(this);
        registeredServices.put(OrchestrationService.class.getName(), orchestrationService);

        SensorsService sensorsService = new SensorsService(this);
        registeredServices.put(SensorsService.class.getName(), sensorsService);

        AtenService atenService = new AtenService(this);
        registeredServices.put(AtenService.class.getName(), atenService);

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

    @Override
    public boolean shutdown() {
        status = Status.Stopping;
        spin.set(false);
        pool.shutdown();
        channel.shutdown();
        status = Status.Stopped;
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return false;
    }

    public Status getStatus() {
        return status;
    }
}
