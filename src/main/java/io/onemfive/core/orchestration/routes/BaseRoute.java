package io.onemfive.core.orchestration.routes;

import io.onemfive.data.Route;

import java.util.Random;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class BaseRoute implements Route {

    protected Boolean routed = false;
    protected Long routeId = new Random(843444628947321731L).nextLong();

    @Override
    public void setId(Long routeId) {
        this.routeId = routeId;
    }

    @Override
    public Long getId() {
        return routeId;
    }

    @Override
    public void setRouted(Boolean routed) {
        this.routed = routed;
    }

    @Override
    public Boolean routed() {
        return routed;
    }
}
