package io.onemfive.core.bus;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageConsumer;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.orchestration.OrchestrationService;
import io.onemfive.core.util.AppThread;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Worker Thread for moving messages from clients to the message channel and then to services and back.
 *
 * @author objectorange
 */
final class WorkerThread extends AppThread {

    private static final Logger LOG = Logger.getLogger(WorkerThread.class.getName());

    private MessageChannel channel;
    private ClientAppManager clientAppManager;
    private Map<String, BaseService> services;

    public WorkerThread(MessageChannel channel, ClientAppManager clientAppManager, Map<String, BaseService> services) {
        super();
        this.channel = channel;
        this.clientAppManager = clientAppManager;
        this.services = services;
    }

    @Override
    public void run() {
        LOG.info(Thread.currentThread().getName() + "Waiting for channel to return message...");
        Envelope e = channel.receive();
        LOG.info(Thread.currentThread().getName() + "Envelope received from channel");
        if (e.replyToClient()) {
            // Service Reply to client
            LOG.info(Thread.currentThread().getName() + "Requesting client notify...");
            clientAppManager.notify(e);
        } else {
            MessageConsumer consumer = null;
            Route route = e.getRoute();
            if(route == null || route.routed()) {
                consumer = services.get(OrchestrationService.class.getName());
            } else {
                consumer = services.get(route.getService());
                if (consumer == null) {
                    // Service name provided is not registered.
                    LOG.warning(Thread.currentThread().getName() + "Route found in header; Service not registered; Please register service: "+route.getService());
                    return;
                }
            }
            boolean received = false;
            int maxSendAttempts = 3;
            int sendAttempts = 0;
            int waitBetweenMillis = 1000;
            while (!received && sendAttempts < maxSendAttempts) {
                if (consumer.receive(e)) {
                    LOG.info(Thread.currentThread().getName() + "Envelope received by service, acknowledging with channel...");
                    channel.ack(e);
                    LOG.info(Thread.currentThread().getName() + "Channel Acknowledged.");
                    received = true;
                } else {
                    synchronized (this) {
                        try {
                            this.wait(waitBetweenMillis);
                        } catch (InterruptedException ex) {

                        }
                    }
                }
                sendAttempts++;
            }
            if(!received) {
                // TODO: Need to move the failed Envelope to a log where it can be retried later
                LOG.warning("Failed 3 attempts to send Envelope (id="+e.getId()+") to Service: ");
            }
        }
    }
}
