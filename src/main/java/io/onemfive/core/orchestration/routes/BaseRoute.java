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
    protected Long correlationId = new Random(843444628947321731L).nextLong();

    @Override
    public void setCorrelationId(Long correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public Long correlationId() {
        return correlationId;
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
