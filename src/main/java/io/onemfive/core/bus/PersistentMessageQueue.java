package io.onemfive.core.bus;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * Provides persistence to ArrayBlockingQueue
 *
 * TODO: Implement and place in Message Channel
 *
 * @author objectorange
 */
final class PersistentMessageQueue<E> extends ArrayBlockingQueue<E> {

    private final Logger LOG = Logger.getLogger(PersistentMessageQueue.class.getName());

    public PersistentMessageQueue(int capacity) {
        super(capacity);
    }

    public PersistentMessageQueue(int capacity, boolean fair) {
        super(capacity, fair);
    }

    public PersistentMessageQueue(int capacity, boolean fair, Collection<? extends E> c) {
        super(capacity, fair, c);
    }

    @Override
    public boolean add(E e) {
        boolean success = super.add(e);
        if(success) {
            // persist

        }
        return success;
    }

    @Override
    public E take() throws InterruptedException {
        E obj = super.take();
            // load

        return obj;
    }

    @Override
    public boolean remove(Object o) {
        boolean success = super.remove(o);
        if(success) {
            // remove

        }
        return success;
    }
}
