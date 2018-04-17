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
    protected Map<String, BaseService> services;

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

    @Override
    public boolean start(Properties properties) {
        if(properties == null)
            properties = this.properties;
        status = Status.Starting;
        boolean startupSuccessful = true;

        channel = new MessageChannel( maxMessagesCached);

        services = new HashMap<>(13);

        // Start Services
        // TODO: Decide which services to start based on implementing solution (e.g. Social/Dgramz)
        // TODO: Start services using SCAppThreads to reduce startup time

        PranaService pranaService = new PranaService(this);
        if(pranaService.start(properties)) {services.put(PranaService.class.getName(), pranaService);}

        ConsensusService consensusService = new ConsensusService(this);
        if(consensusService.start(properties)) {services.put(ConsensusService.class.getName(),consensusService);}

        ContentService contentService = new ContentService(this);
        if(contentService.start(properties)) {services.put(ContentService.class.getName(),contentService);}

        DEXService dexService = new DEXService(this);
        if(dexService.start(properties)) {services.put(DEXService.class.getName(),dexService);}

        RepositoryService repositoryService = new RepositoryService(this);
        if(repositoryService.start(properties)) {services.put(RepositoryService.class.getName(), repositoryService);}

        InfoVaultService infoVaultService = new InfoVaultService(this);
        if(infoVaultService.start(properties)) {services.put(InfoVaultService.class.getName(),infoVaultService);}

        KeyRingService keyRingService = new KeyRingService(this);
        if(keyRingService.start(properties)) {services.put(KeyRingService.class.getName(),keyRingService);}

        LIDService lidService = new LIDService(this);
        if(lidService.start(properties)) {services.put(LIDService.class.getName(),lidService);}

        OrchestrationService orchestrationService = new OrchestrationService(this);
        if(orchestrationService.start(properties)) {services.put(OrchestrationService.class.getName(),orchestrationService);}

        SensorsService sensorsService = new SensorsService(this);
        if(sensorsService.start(properties)) {services.put(SensorsService.class.getName(),sensorsService);}

        AtenService atenService = new AtenService(this);
        if(atenService.start(properties)) {services.put(AtenService.class.getName(), atenService);}

        pool = new WorkerThreadPool(clientAppManager, services, channel, maxThreads, maxThreads, properties);
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
