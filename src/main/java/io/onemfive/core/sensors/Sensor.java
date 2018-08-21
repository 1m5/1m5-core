package io.onemfive.core.sensors;

import io.onemfive.core.LifeCycle;
import io.onemfive.data.Envelope;
import io.onemfive.data.Peer;

import java.util.Map;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface Sensor extends LifeCycle {
    boolean send(Envelope envelope);
    boolean reply(Envelope envelope);
    SensorStatus getStatus();
    Integer getRestartAttempts();
    Map<String,Peer> getPeers();
}
