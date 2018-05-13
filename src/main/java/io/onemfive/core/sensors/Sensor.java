package io.onemfive.core.sensors;

import io.onemfive.core.LifeCycle;
import io.onemfive.data.Envelope;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface Sensor extends LifeCycle {
    boolean send(Envelope envelope);
}
