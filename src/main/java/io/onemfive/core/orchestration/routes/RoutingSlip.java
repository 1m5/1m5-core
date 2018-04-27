package io.onemfive.core.orchestration.routes;

import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class RoutingSlip extends SimpleRoute {

    protected List<Route> routes = new ArrayList<>();
    protected int currentRouteIndex = 0;

    public RoutingSlip(String service, String operation) {
        super(service, operation);
    }

    public RoutingSlip(Envelope envelope, String service, String operation) {
        super(envelope, service, operation);
    }

    public RoutingSlip(Envelope envelope, String service, String operation, List<Route> routes) {
        super(envelope, service, operation);
        this.routes = routes;
    }

    protected void addRoute(Route route) throws Exception {
        routes.add(route);
    }

    public Route nextRoute(Envelope envelope) {
        if(currentRouteIndex < routes.size()) {
            return routes.get(currentRouteIndex++);
        } else {
            return null;
        }
    }
}
