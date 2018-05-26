package io.onemfive.core.bus;

import io.onemfive.core.MessageProducer;
import io.onemfive.data.Envelope;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
final class MessageChannel implements MessageProducer {

    private final Logger LOG = Logger.getLogger(MessageChannel.class.getName());

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
        System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Removing Envelope-"+envelope.getId()+"("+envelope+") from message queue (size="+queue.size()+")");
        queue.remove(envelope);
        System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Removed Envelope-"+envelope.getId()+"("+envelope+") from message queue (size="+queue.size()+")");
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
                    System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Envelope-"+envelope.getId()+"("+envelope+") added to message queue (size="+queue.size()+")");
                return success;
            } catch (IllegalStateException e) {
                System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Channel at capacity; rejected Envelope-"+envelope.getId()+"("+envelope+").");
                return false;
            }
        } else {
            System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Not accepting envelopes yet.");
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
            System.out.println(MessageChannel.class.getSimpleName()+": "+Thread.currentThread().getName()+": Got Envelope-"+next.getId()+"("+next+") (queue size="+queue.size()+")");
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
