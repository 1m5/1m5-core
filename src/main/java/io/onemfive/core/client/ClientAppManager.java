package io.onemfive.core.client;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.data.Envelope;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Manages Client Application interaction with the 1M5 service.
 *
 * @author objectorange
 */
public final class ClientAppManager {

    public enum Status {STOPPED, INITIALIZING, READY}

    private Status status = Status.STOPPED;
    private boolean shutdownOnLastUnregister = true;

    private OneMFiveAppContext context;
    private MessageProducer producer;
    private Client client;

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

    /**
     * Initializes the client app manager if its status is STOPPED.
     * When client app manager is starting, the sc service
     * will be started if its status is STOPPED also.
     * Once started, its is ready for building an Client.
     * @return non-null
     */
    public boolean initialize() {
        System.out.println(ClientAppManager.class.getSimpleName()+": requesting instance...");
        if(status == Status.STOPPED) {
            System.out.println(ClientAppManager.class.getSimpleName()+": initializing...");
            status = Status.INITIALIZING;
            context = OneMFiveAppContext.getInstance();
            ServiceBus serviceBus = context.getServiceBus();
            if (serviceBus.getStatus() == ServiceBus.Status.Stopped) {
                System.out.println("Starting Service Bus...");
                serviceBus.start(null);
                System.out.println("Service Bus started.");
            }
            System.out.println("Service Bus running.");
            // Assign service bus to producer for sending messages to service bus
            producer = serviceBus;
            status = Status.READY;
            System.out.println(ClientAppManager.class.getSimpleName()+": ready");
        }
        client = buildClient();
        return true;
    }

    /**
     * Shuts down the client app manager instance and the 1M5 service.
     */
    public boolean stop() {
        System.out.println(ClientAppManager.class.getSimpleName()+": shutting down...");
        boolean isStopped = false;
        if(status == Status.READY) {
            ServiceBus serviceBus = context.getServiceBus();
            if(serviceBus.getStatus() == ServiceBus.Status.Running) {
                isStopped = serviceBus.shutdown();
            }
            registered.clear();
        }
        System.out.println(ClientAppManager.class.getSimpleName()+": shutdown");
        if(isStopped) status = Status.STOPPED;
        return isStopped;
    }

    public Client getClient(boolean defaultInstance) {
        if(client == null) {
            client = buildClient();
            return client;
        }
        if(defaultInstance) {
            return client;
        }
        return buildClient();
    }

    /**
     * Builds an Client.
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
     *  Called by WorkerThread to notify client of reply.
     *  If another object would happen to call this,
     *  it will just ignore the call.
     *
     *  @param e non-null
     */
    public void notify(Envelope e) {
        if(e != null) {
            Long clientId = e.getClient();
            System.out.println(ClientAppManager.class.getSimpleName()+": Client.id="+clientId);
            Client client = getRegisteredApp(clientId);
            if (client != null) {
                System.out.println(ClientAppManager.class.getSimpleName()+": Found client; notifying...");
                client.notify(e);
            } else {
                System.out.println(ClientAppManager.class.getSimpleName()+": Client not found. Number of registered clients: "+registered.size());
            }
        }
    }

    /**
     *  Unregister with the manager.
     *  If last client registered and shutdownOnLastUnregister is true,
     *  SC service will automatically stop.
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
     *  @return client app or null if not found
     */
    public Client getRegisteredApp(Long id) {
        if(id == null)
            return null;
        else
            return registered.get(id);
    }

}
