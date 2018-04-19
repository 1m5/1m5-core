package io.onemfive.core.bus;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Provides persistence to ArrayBlockingQueue
 *
 * TODO: Implement and place in Message Channel
 *
 * @author objectorange
 */
class PersistentMessageQueue<E> extends ArrayBlockingQueue<E> {

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

        return obj;
    }

    @Override
    public boolean remove(Object o) {
        boolean success = super.remove(o);

        return success;
    }
}
