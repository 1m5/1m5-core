package io.onemfive.core.util;

/**
 * Simple interface for events to be queued up and notified on expiration
 */
public interface TimerEvent {
    /**
     * the time requested has been reached (this call should NOT block,
     * otherwise the whole SimpleTimer gets backed up)
     *
     */
    void timeReached();
}
