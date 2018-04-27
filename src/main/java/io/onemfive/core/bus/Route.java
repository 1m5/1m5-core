package io.onemfive.core.bus;

import io.onemfive.data.Envelope;

/**
 * A service and operation.
 *
 * @author objectorange
 */
public interface Route {
    String getService();
    String getOperation();
    void setEnvelope(Envelope envelope);
}
