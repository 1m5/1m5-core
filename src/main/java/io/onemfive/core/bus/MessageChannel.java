package io.onemfive.core.bus;

import io.onemfive.core.MessageProducer;
import io.onemfive.data.Envelope;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
final class MessageChannel implements MessageProducer {

    private boolean accepting = false;
    private BlockingQueue<Envelope> queue;
    // Capacity until blocking occurs
    private int capacity;

    MessageChannel(int capacity) {
        this.capacity = capacity;
        queue = new ArrayBlockingQueue<>(capacity);
        accepting = true;
    }

    BlockingQueue<Envelope> getQueue() {
        return queue;
    }

    void ack(Envelope envelope) {
        queue.remove(envelope);
    }

    /**
     * Send message on channel.
     * @param envelope
     */
    public boolean send(Envelope envelope) {
        if(accepting) {
            try {
                boolean success = queue.add(envelope);
                if(success)
                    System.out.println(MessageChannel.class.getSimpleName()+": Envelope-"+envelope.getId()+" added to message queue.");
                return success;
            } catch (IllegalStateException e) {
                System.out.println(MessageChannel.class.getSimpleName()+": Channel at capacity; rejected envelope.");
                return false;
            }
        } else {
            System.out.println(MessageChannel.class.getSimpleName()+": Not accepting envelopes yet.");
            return false;
        }
    }

    /**
     * Receive envelope from channel with blocking.
     * @return
     */
    public Envelope receive() {
        Envelope next = null;
        try {
            System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Requesting envelope from message queue, blocking...");
            next = queue.take();
            System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Got envelope-"+next.getId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return next;
    }

    /**
     * Receive envelope from channel with blocking until timeout.
     * @param timeout in milliseconds
     * @return
     */
    public Envelope receive(int timeout) {
        Envelope next = null;
        try {
            queue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return next;
    }

    boolean start(Properties properties) {
        queue = new ArrayBlockingQueue<>(capacity);
        return true;
    }

    boolean pause() {
        return false;
    }

    boolean resume() {
        return false;
    }

    boolean restart() {
        return false;
    }

    boolean shutdown() {
        accepting = false;
        // TODO: wait for all messages to process
        return true;
    }

    boolean forceShutdown() {

        return false;
    }
}
