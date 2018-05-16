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

    public boolean addRoute(SimpleRoute route) {
        route.setId(this.routeId);
        routes.add(route);
        return true;
    }

    @Override
    public Boolean routed() {
        return routes.get(currentRouteIndex).routed();
    }

    public Route currentRoute() {
        return routes.get(currentRouteIndex);
    }

    public Route nextRoute(Envelope e) {
        Route r = null;
        if(currentRouteIndex + 1 < routes.size()) {
            r = routes.get(++currentRouteIndex);
            r.setEnvelope(e);
        }
        return r;
    }
}
