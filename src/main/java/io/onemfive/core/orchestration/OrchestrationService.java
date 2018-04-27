package io.onemfive.core.orchestration;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
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

    private Map<String,Route> routes;

    public OrchestrationService(MessageProducer producer) {
        super(producer);
    }

    /**
     * If service is Orchestration and there is no Route, then the service needs to figure out what route to take.
     * If service is Orchestration and there is a Route, then the service calls the next Route
     *
     * @param envelope
     */
    public void handleDocument(Envelope envelope) {
        route(envelope);
        System.out.println("Received document by Orchestration Service.");
    }

    public void handleEvent(Envelope envelope) {
        route(envelope);
        System.out.println("Received event by Orchestration Service.");
    }

    private void route(Envelope envelope) {
        Route route = (Route)envelope.getHeader(Envelope.ROUTE);
        // Build Route and send to channel
        if(route == null) {
            // No route
            System.out.println("Orchestration Service doesn't handle Envelopes with no Route for now.");
            deadLetter(envelope);
        } else {
            // execute route
            if(route instanceof RoutingSlip) {
                Route nextRoute = ((RoutingSlip)route).nextRoute(envelope);
                if (nextRoute == null) {
                    envelope.setHeader(Envelope.REPLY, true);
                } else {
                    envelope.setHeader(Envelope.ROUTE, nextRoute);
                }
                reply(envelope);
            } else if(route instanceof SimpleRoute) {
                Route to = routes.get(route.getService()+"."+route.getOperation());
                if(to != null) {
                    envelope.setHeader(Envelope.ROUTE, to);
                    reply(envelope);
                } else {
                    deadLetter(envelope);
                }
            } else {
                System.out.println("Orchestration Service doesn't handle other routes besides Routing Slip and Simple Route.");
                deadLetter(envelope);
            }
        }
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("Starting OrchestrationService...");
        boolean startupSuccessful = true;
        routes = new HashMap<>();
        // Build Routes
        try {
            Properties props = Config.loadFromClasspath("orchestration.config",properties);
            String routesString = props.getProperty("routes");
            if(routesString != null) {
                String[] routeStrings = routesString.split(",");
                Route route;
                for(String routeString : routeStrings) {
                    route = (Route)Class.forName(routeString).newInstance();
                    routes.put(route.getService()+"."+route.getOperation(),route);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("OrchestrationService started.");
        return startupSuccessful;
    }

}
