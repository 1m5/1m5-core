package io.onemfive.core.orchestration.routes;

import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class SimpleRoute implements Route {

    protected Envelope envelope;
    protected String service;
    protected String operation;

    public SimpleRoute(String service, String operation) {
        this.service = service;
        this.operation = operation;
    }

    public SimpleRoute(Envelope envelope, String service, String operation) {
        this.envelope = envelope;
        this.service = service;
        this.operation = operation;
    }

    @Override
    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getOperation() {
        return operation;
    }
}
