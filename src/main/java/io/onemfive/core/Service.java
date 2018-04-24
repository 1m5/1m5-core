package io.onemfive.core;

import io.onemfive.data.Envelope;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface Service {
    void handleDocument(Envelope envelope);
    void handleEvent(Envelope envelope);
    void handleCommand(Envelope envelope);
    void handleHeaders(Envelope envelope);
}
