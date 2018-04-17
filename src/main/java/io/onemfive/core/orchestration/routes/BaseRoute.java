package io.onemfive.core.orchestration.routes;

import io.onemfive.core.orchestration.Route;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
abstract class BaseRoute implements Route {

    protected String service;
    protected String operation;

    BaseRoute(String service, String operation) {
        this.service = service;
        this.operation = operation;
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
