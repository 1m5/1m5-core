package io.onemfive.core.bus;

import io.onemfive.core.LifeCycle;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;

import java.util.Date;
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
final class MessageChannel implements MessageProducer, LifeCycle {

    private static final Logger LOG = Logger.getLogger(MessageChannel.class.getName());

    private boolean accepting = false;
    private BlockingQueue<Envelope> queue;
    // Capacity until blocking occurs
    private int capacity;

    MessageChannel(int capacity) {
        this.capacity = capacity;
    }

    BlockingQueue<Envelope> getQueue() {
        return queue;
    }

    void ack(Envelope envelope) {
        LOG.finest(Thread.currentThread().getName()+": Removing Envelope-"+envelope.getId()+"("+envelope+") from message queue (size="+queue.size()+")");
        queue.remove(envelope);
        LOG.finest(Thread.currentThread().getName()+": Removed Envelope-"+envelope.getId()+"("+envelope+") from message queue (size="+queue.size()+")");
    }

    /**
     * Send message on channel.
     * @param e Envelope
     */
    public boolean send(Envelope e) {
        if(accepting) {
            try {
                boolean success = queue.add(e);
                if(success)
                    LOG.finest(Thread.currentThread().getName()+": Envelope-"+e.getId()+"("+e+") added to message queue (size="+queue.size()+")");
                return success;
            } catch (IllegalStateException ex) {
                String errMsg = Thread.currentThread().getName()+": Channel at capacity; rejected Envelope-"+e.getId()+"("+e+").";
                DLC.addErrorMessage(errMsg, e);
                LOG.warning(errMsg);
                return false;
            }
        } else {
            String errMsg = Thread.currentThread().getName()+": Not accepting envelopes yet.";
            DLC.addErrorMessage(errMsg, e);
            LOG.warning(errMsg);
            return false;
        }
    }

    /**
     * Receive envelope from channel with blocking.
     * @return Envelope
     */
    public Envelope receive() {
        Envelope next = null;
        try {
            LOG.finest(Thread.currentThread().getName()+": Requesting envelope from message queue, blocking...");
            next = queue.take();
            LOG.finest(Thread.currentThread().getName()+": Got Envelope-"+next.getId()+"("+next+") (queue size="+queue.size()+")");
        } catch (InterruptedException e) {
            // No need to log
        }
        return next;
    }

    /**
     * Receive envelope from channel with blocking until timeout.
     * @param timeout in milliseconds
     * @return Envelope
     */
    public Envelope receive(int timeout) {
        Envelope next = null;
        try {
            queue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // No need to log
        }
        return next;
    }

    public boolean start(Properties properties) {
        queue = new ArrayBlockingQueue<>(capacity);
        accepting = true;
        return true;
    }

    public boolean pause() {
        return false;
    }

    public boolean unpause() {
        return false;
    }

    public boolean restart() {
        return false;
    }

    public boolean shutdown() {
        accepting = false;
        long begin = new Date().getTime();
        long runningTime = begin;
        long waitMs = 1000;
        long maxWaitMs = 3 * 1000; // only 3 seconds
        while(queue.size() > 0 && runningTime < maxWaitMs) {
            waitABit(waitMs);
            runningTime += waitMs;
        }
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        accepting = false;
        long begin = new Date().getTime();
        long runningTime = begin;
        long waitMs = 1000;
        long maxWaitMs = 30 * 1000; // up to 30 seconds
        while(queue.size() > 0 && runningTime < maxWaitMs) {
            waitABit(waitMs);
            runningTime += waitMs;
        }
        return true;
    }

    boolean forceShutdown() {
        return shutdown();
    }

    private void waitABit(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {}
    }
}
