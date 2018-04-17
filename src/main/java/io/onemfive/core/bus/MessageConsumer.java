package io.onemfive.core.bus;

import io.onemfive.data.Envelope;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface MessageConsumer {
    boolean receive(Envelope envelope);
}
