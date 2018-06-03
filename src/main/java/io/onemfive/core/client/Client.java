package io.onemfive.core.client;

import io.onemfive.data.Envelope;
import io.onemfive.data.ServiceCallback;

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

}
