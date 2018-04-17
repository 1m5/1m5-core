package io.onemfive.core.orchestration;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */

import io.onemfive.data.Envelope;

/**
 * A route across micro-services within the platform.
 */

public interface Route {
    Route next(Envelope envelope);
    String getService();
    String getOperation();
}
