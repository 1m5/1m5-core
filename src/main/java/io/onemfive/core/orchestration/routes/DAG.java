package io.onemfive.core.orchestration.routes;

import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class DAG extends RoutingSlip {

    public DAG(String service, String operation) {
        super(service, operation);
    }

    public DAG(Envelope envelope, String service, String operation) {
        super(envelope, service, operation);
    }

    public boolean addRoute(SimpleRoute route) {
        // Ensure no Service is called twice (Acyclic)
        if(acyclic(route)) {
            routes.add(route);
            return true;
        } else {
            return false;
        }
    }

    private boolean acyclic(Route newRoute) {
        for(Route r : routes) {
            if(r.getService().equals(newRoute.getService())) return false;
        }
        return true;
    }
}
