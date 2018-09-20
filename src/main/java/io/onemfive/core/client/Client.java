package io.onemfive.core.client;

import io.onemfive.core.bus.BusStatusListener;
import io.onemfive.core.notification.SubscriptionRequest;
import io.onemfive.data.Envelope;
import io.onemfive.data.EventMessage;
import io.onemfive.data.ServiceCallback;
import io.onemfive.data.Subscription;

/**
 * Define the standard means of interacting with the 1M5 application when embedded.
 *
 * Never ever hold a static reference to the context or anything derived from it.
 *
 * @author objectorange
 */
public interface Client {

    /**
     * Request to 1M5 application with no reply (fire-and-forget).
     * @param envelope non-null Envelope
     * @see io.onemfive.data.Envelope
     */
    void request(Envelope envelope);

    /**
     * Request to 1M5 application with a reply using a ServiceCallback.
     * @param envelope non-null Envelope
     * @param cb non-null ServiceCallback
     * @see io.onemfive.data.Envelope
     * @see io.onemfive.data.ServiceCallback
     */
    void request(Envelope envelope, ServiceCallback cb);


    /**
     * Notify client of reply.
     * @param envelope non-null Envelope
     * @see io.onemfive.data.Envelope
     */
    void notify(Envelope envelope);

    /**
     *  The ID of the client assigned during creation.
     *  @return non-null Long
     */
    Long getId();

    /**
     * Register a ClientStatusListener so that Clients can act on Client status changes.
     * @param listener
     */
    void registerClientStatusListener(ClientStatusListener listener);

    /**
     * Subscribe to events by Type.
     * @param subscription
     */
    void subscribeToEvent(EventMessage.Type eventType, Subscription subscription);

    /**
     * Subscribe to Email events (receiving Email).
     * @param subscription
     */
    void subscribeToEmail(Subscription subscription);

}
