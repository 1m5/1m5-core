package io.onemfive.core.client;

import io.onemfive.data.Envelope;
import io.onemfive.data.ServiceCallback;

/**
 * Define the standard means of interacting with the SC system.
 *
 *  Never ever hold a static reference to the context or anything derived from it.
 *
 * @author objectorange
 */
public interface Client {

    /**
     * Request to SC system with no reply (fire-and-forget).
     * @param envelope non-null
     */
    void request(Envelope envelope);

    /**
     * Request to SC system with a reply using a Callback.
     * @param envelope non-null
     * @param cb non-null
     */
    void request(Envelope envelope, ServiceCallback cb);


    /**
     * Notify client of reply.
     * @param envelope
     */
    void notify(Envelope envelope);

    /**
     *  The ID of the client assigned during creation.
     *  @return non-null
     */
    Long getId();

}
