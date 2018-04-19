package io.onemfive.core;

import io.onemfive.data.Envelope;

/**
 * Sends messages.
 *
 * @author objectorange
 */
public interface MessageProducer {
    /**
     *
     * @param envelope
     * @return
     */
    boolean send(Envelope envelope);
}
