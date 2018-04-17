package io.onemfive.core.orchestration;

import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.MessageProducer;
import io.onemfive.core.orchestration.routes.RouteBuilder;
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
     * If service is Orchestration adn there is a Route, then the service calls the next Route
     *
     * @param envelope
     */
    public void handleDocument(Envelope envelope) {
        String serviceName = (String)envelope.getHeader(Envelope.SERVICE);
        String routeName = (String)envelope.getHeader(Envelope.ROUTE);
        // Build Route and send to channel
        if(routeName == null) {

            // Likely an external call; route to SensorService

        } else {
            // execute route

        }
        System.out.println("Received document by Orchestration Service.");
    }

    public void handleEvent(Envelope envelope) {
        EventMessage message = (EventMessage)envelope.getMessage();
        // TODO: handle events
        Route route = RouteBuilder.build(envelope);
        System.out.println("Received event by Orchestration Service.");
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("Starting OrchestrationService...");
        boolean startupSuccessful = true;
        routes = new HashMap<>();
        // Build Routes
        buildRoutes();
        System.out.println("OrchestrationService started.");
        return startupSuccessful;
    }

    private void buildRoutes() {
        // TODO: Build Routes from routes.xml
        System.out.println("Building routes in Orchestration Service.");

        System.out.println("Routes in Orchestration Service built.");
    }

}
