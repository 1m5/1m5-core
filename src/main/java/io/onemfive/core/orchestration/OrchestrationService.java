package io.onemfive.core.orchestration;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ipfs.IPFSService;
import io.onemfive.data.*;

import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Orchestrating services based on configurable route patterns.
 *
 * @author objectorange
 */
public class OrchestrationService extends BaseService {

    private static final Logger LOG = Logger.getLogger(OrchestrationService.class.getName());

    private boolean starting = false;
    private boolean running = false;
    private boolean shuttingDown = false;
    private boolean shutdown = false;

    private int activeRoutes = 0;
    private int remainingRoutes = 0;

    private final Object lock = new Object();

    public OrchestrationService(MessageProducer producer) {
        super(producer);
        orchestrator = true;
    }

    /**
     * If service is Orchestration and there is no Route, then the service needs to figure out what route to take.
     * If service is Orchestration and there is a Route, then the service calls the next Route
     *
     * @param e
     */
    @Override
    public void handleDocument(Envelope e) {
        LOG.info("Received document by Orchestration Service; routing...");
        route(e);
    }

    @Override
    public void handleEvent(Envelope e) {
        LOG.info("Received event by Orchestration Service; routing...");
        route(e);
    }

    @Override
    public void handleHeaders(Envelope e) {
        LOG.info("Received headers by Orchestration Service; routing...");
        route(e);
    }

    private void route(Envelope e) {
        if(running) {
            RoutingSlip rs = e.getDynamicRoutingSlip();
            Route route = e.getRoute();
            // Select Next Route and send to channel

            if(!rs.inProgress()) {
                // new slip
                remainingRoutes += rs.numberRemainingRoutes();
                rs.start();
            }
            if(rs.peekAtNextRoute() != null) {
                // slip has routes left, set next route
                route = rs.nextRoute();
                if(OrchestrationService.class.getName().equals(route.getService())) {
                    // need to determine what next route is
                    URL url = e.getURL();
                    if(url != null && url.getPath() != null) {
                        String path = url.getPath();
                        if(path.startsWith("/ipfs/")) {
                            route = new SimpleRoute(IPFSService.class.getName(), null);
                        }
                    }
                }
                e.setRoute(route);
                reply(e);
                activeRoutes++;
            } else if(route == null || route.routed() || OrchestrationService.class.getName().equals(route.getService())) {
                // no routes left
                if(e.getClient() != null) {
                    // is a client request so flag for reply to client
                    e.setReplyToClient(true);
                    reply(e);
                } else {
                    // not a client request so just end
                    endRoute(e);
                }
                activeRoutes--;
                remainingRoutes--;
            } else {
                // route is not null, hasn't been routed, and is not for Orchestration Service so one-way fire-and-forget -> Send on its way
                reply(e);
                activeRoutes++;
                remainingRoutes++;
            }
        } else {
            LOG.info("Not running.");
            deadLetter(e);
        }
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        shuttingDown = false;
        shutdown = false;
        starting = true;

        activeRoutes = 0;
        remainingRoutes = 0;

        running = true;
        starting = false;
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        running = false;
        shuttingDown = true;
        // Give it 3 seconds
        int tries = 1;
        while(remainingRoutes > 0 && tries > 0) {
            waitABit(3 * 1000);
            tries--;
        }
        shutdown = true;
        shuttingDown = false;
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        running = false;
        shuttingDown = true;
        // Give it 30 seconds
        int tries = 10;
        while(remainingRoutes > 0 && tries > 0) {
            waitABit(3 * 1000);
            tries--;
        }
        shutdown = true;
        shuttingDown = false;
        return true;
    }

    private void waitABit(long waitTime) {
        synchronized (lock) {
            try {
                this.wait(waitTime);
            } catch (InterruptedException e) {

            }
        }
    }

}
