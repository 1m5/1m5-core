package io.onemfive.core.client;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.bus.BusStatusListener;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.data.Envelope;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Manages Client Application interaction with the 1M5 service.
 *
 * @author objectorange
 */
public final class ClientAppManager implements BusStatusListener {

    private static final Logger LOG = Logger.getLogger(ClientAppManager.class.getName());

    public enum Status {STOPPED, INITIALIZING, READY, STOPPING}

    private Status status = Status.STOPPED;
    private boolean shutdownOnLastUnregister = true;

    private OneMFiveAppContext context;
    private MessageProducer producer;
    private Client defaultClient;

    // registered name to client
    protected final Map<Long, Client> registered;

    public ClientAppManager(boolean shutdownOnLastUnregister) {
        this.shutdownOnLastUnregister = shutdownOnLastUnregister;
        registered = new HashMap<>(20);
    }

    public void setShutdownOnLastUnregister(boolean shutdownOnLastUnregister) {
        this.shutdownOnLastUnregister = shutdownOnLastUnregister;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void busStatusChanged(ServiceBus.Status busStatus) {
        switch (busStatus) {
            case Starting: {
                status = Status.INITIALIZING;
                LOG.info("ClientAppManager Initializing...");
                break;
            }
            case Running: {
                status = Status.READY;
                LOG.info("ClientAppManager Running");
                break;
            }
            case Stopping: {
                status = Status.STOPPING;
                LOG.info("ClientAppManager Stopping...");
                break;
            }
            case Stopped: {
                status = Status.STOPPED;
                LOG.info("ClientAppManager Stopped");
                break;
            }
        }
        Collection<Client> clients = registered.values();
        for(Client c : clients) {
            if(c instanceof SimpleClient) {
                ((SimpleClient)c).updateClientStatus(status);
            }
        }
    }

    /**
     * Initializes the defaultClient app manager if its status is STOPPED.
     * When defaultClient app manager is starting, the sc service
     * will be started if its status is STOPPED also.
     * Once started, its is ready for building a Client.
     * @return non-null
     */
    public boolean initialize() {
        LOG.finer("Requesting instance...");
        if(status == Status.STOPPED) {
            LOG.finer("initializing...");
            status = Status.INITIALIZING;
            context = OneMFiveAppContext.getInstance();
            ServiceBus serviceBus = context.getServiceBus();
            serviceBus.registerBusStatusListener(this);
            if (serviceBus.getStatus() == ServiceBus.Status.Stopped) {
                LOG.info("Starting 1M5 Service Bus...");
                // TODO: Add properties to ServiceBus start
                serviceBus.start(null);
            }
            // Assign service bus to producer for sending messages to service bus
            producer = serviceBus;
            status = Status.READY;
            LOG.info("1M5 Service Bus running ready for requests.");
        }
        defaultClient = buildClient();
        return true;
    }

    /**
     * Shuts down the defaultClient app manager instance and the 1M5 service.
     */
    public boolean stop() {
        LOG.info("Shutting down...");
        boolean isStopped = false;
        if(status == Status.READY) {
            ServiceBus serviceBus = context.getServiceBus();
            if(serviceBus.getStatus() == ServiceBus.Status.Running) {
                isStopped = serviceBus.shutdown();
            }
            registered.clear();
        }
        LOG.info("Shutdown");
        if(isStopped) status = Status.STOPPED;
        return isStopped;
    }

    public Client getClient(boolean defaultInstance) {
        if(defaultInstance) {
            if(defaultClient == null)
                defaultClient = buildClient();
            return defaultClient;
        }
        return buildClient();
    }

    /**
     * Builds a Client.
     * Please call unregister() when finished using it
     * otherwise with excess creation, it can lead to
     * a memory 'leak'.
     * @return non-null
     */
    private Client buildClient() {
        Client client = new SimpleClient(new Random(758307813189741L).nextLong(), producer);
        registered.put(client.getId(), client);
        return client;
    }

    /**
     *  Called by WorkerThread to notify defaultClient of reply.
     *  If another object would happen to call this,
     *  it will just ignore the call.
     *
     *  @param e non-null
     */
    public void notify(Envelope e) {
        if(e != null) {
            Client client;
            Long clientId = e.getClient();
            if(clientId == null) {
                // Likely coming from notifications therefore use default client as there should normally only be one Client
                client = defaultClient;
            } else {
                client = getRegisteredClient(clientId);
            }
            if (client != null) {
                client.notify(e);
            } else {
                LOG.warning("Client not found. Number of registered clients: "+registered.size());
            }
        }
    }

    /**
     *  Unregister with the manager.
     *  If last defaultClient registered and shutdownOnLastUnregister is true,
     *  1M5 service will automatically stop.
     *
     *  @param client non-null
     */
    public void unregister(Client client) {
        registered.remove(client.getId());
        if(registered.size() == 0 && shutdownOnLastUnregister) {
            stop();
        }
    }

    public int numberRegistered() {
        return registered.size();
    }

    /**
     *  Get a registered app.
     *  Only used for apps finding other apps.
     *  Do not hold a static reference.
     *  If you only need to find a port, use the PortMapper instead.
     *
     *  @param id non-null
     *  @return defaultClient app or null if not found
     */
    public Client getRegisteredClient(Long id) {
        if(id == null)
            return null;
        else
            return registered.get(id);
    }

}
