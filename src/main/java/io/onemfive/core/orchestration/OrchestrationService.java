package io.onemfive.core.orchestration;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.orchestration.routes.RoutingSlip;
import io.onemfive.core.orchestration.routes.SimpleRoute;
import io.onemfive.data.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestrating services based on configurable route patterns.
 *
 * @author objectorange
 */
public class OrchestrationService extends BaseService {

    private boolean starting = false;
    private boolean running = false;
    private boolean shuttingDown = false;
    private boolean shutdown = false;
    // In-Progress Routing Slips
    // TODO: Persist slips
    private Map<Long,RoutingSlip> slips;

    private final Object lock = new Object();

    public OrchestrationService(MessageProducer producer) {
        super(producer);
    }

    /**
     * If service is Orchestration and there is no Route, then the service needs to figure out what route to take.
     * If service is Orchestration and there is a Route, then the service calls the next Route
     *
     * @param e
     */
    @Override
    public void handleDocument(Envelope e) {
        System.out.println("Received document by Orchestration Service; routing...");
        route(e);
    }

    @Override
    public void handleEvent(Envelope e) {
        System.out.println("Received event by Orchestration Service; routing...");
        route(e);
    }

    @Override
    public void handleHeaders(Envelope e) {
        System.out.println("Received headers by Orchestration Service; routing...");
        route(e);
    }

    private void route(Envelope e) {
        if(running) {
            Route route = (Route) e.getHeader(Envelope.ROUTE);
            // Build Route and send to channel
            if (route == null) {
                // No route
                System.out.println("Orchestration Service doesn't handle Envelopes with no Route for now.");
                deadLetter(e);
            } else {
                // Routing Slip
                if (route instanceof RoutingSlip) {
                    // New routing slip requested
                    RoutingSlip slip = (RoutingSlip) route;
                    slips.put(slip.correlationId(), slip);
                    e.setHeader(Envelope.ROUTE, slip.currentRoute());
                    reply(e);
                } else if (route instanceof SimpleRoute) {
                    if (slips.containsKey(route.correlationId())) {
                        // Route from Routing Slip
                        route.setRouted(true);
                        RoutingSlip slip = slips.get(route.correlationId());
                        route = slip.nextRoute(e);
                        if (route == null) {
                            // End of slip
                            slip.setRouted(true);
                            if(e.getHeader(Envelope.CLIENT_REPLY_ACTION)!= null) {
                                e.setHeader(Envelope.CLIENT_REPLY, true);
                                e.setHeader(Envelope.ROUTE, slip);
                                reply(e);
                            } else {
                                // End of the line for the routing slip and no reply to client
                                System.out.println("Routing Slip finished with no client reply.");
                            }
                        } else {
                            // Forward to next route
                            e.setHeader(Envelope.ROUTE, route);
                            reply(e);
                        }
                    } else {
                        if(e.getHeader(Envelope.REPLY) == null) {
                            // Forward onto service
                            reply(e);
                        } else {
                            // Coming back from service
                            if(e.getHeader(Envelope.CLIENT_REPLY_ACTION) != null) {
                                e.setHeader(Envelope.CLIENT_REPLY, true);
                                reply(e);
                            } else {
                                // End of the line for the simple route and no reply to client
                                System.out.println("Simple Route finished with no client reply.");
                            }
                        }
                    }
                } else {
                    System.out.println("Orchestration Service doesn't handle other routes besides Routing Slip and Simple Route.");
                    deadLetter(e);
                }
            }
        } else {
            System.out.println("Orchestration Service not running.");
            deadLetter(e);
        }
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("Starting OrchestrationService...");
        shuttingDown = false;
        shutdown = false;
        starting = true;
        slips = new HashMap<>();
        running = true;
        starting = false;
        System.out.println("OrchestrationService started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        running = false;
        slips = null;
        shutdown = true;
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        running = false;
        shuttingDown = true;
        int tries = 1;
        while(slips.size() > 0 && tries > 0) {
            waitABit(2 * 1000);
            tries--;
        }
        slips = null;
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
