package io.onemfive.core.bus;

import io.onemfive.core.BaseService;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.util.AppThread;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Thread pool for WorkerThreads.
 *
 * TODO: Improve teardown
 * TODO: Improve configuration options
 *
 * @author objectorange
 */
final class WorkerThreadPool extends AppThread {

    private static final Logger LOG = Logger.getLogger(WorkerThreadPool.class.getName());

    public enum Status {Starting, Running, Stopping, Stopped}

    private Status status = Status.Stopped;

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final ClientAppManager clientAppManager;
    private Map<String,BaseService> services;
    private MessageChannel channel;
    private ExecutorService pool;
    private int poolSize = NUMBER_OF_CORES * 2; // default
    private int maxPoolSize = NUMBER_OF_CORES * 2; // default
    private Properties properties;
    private AtomicBoolean spin = new AtomicBoolean(true);

    WorkerThreadPool(ClientAppManager clientAppManager, Map<String, BaseService> services, MessageChannel channel, int poolSize, int maxPoolSize, Properties properties) {
        this.clientAppManager = clientAppManager;
        this.services = services;
        this.channel = channel;
        this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;
        this.properties = properties;
    }

    @Override
    public void run() {
        startPool();
        status = Status.Stopped;
    }

    private boolean startPool() {
        int index = 0;
        status = Status.Starting;
        pool = Executors.newFixedThreadPool(maxPoolSize);
        status = Status.Running;
        final long printPeriodMs = 5000; // print * every 5 seconds
        final long waitPeriodMs = 500; // wait half a second
        long currentWait = 0;
        while(spin.get()) {
            synchronized (this){
                try {
                    if(currentWait > printPeriodMs) {
                        LOG.finest("*");
                        currentWait = 0;
                    }
                    int queueSize = channel.getQueue().size();
                    if(queueSize > 0) {
                        LOG.finest("Queue Size = "+queueSize+" : Launching thread...");
                        pool.execute(new WorkerThread(channel, clientAppManager, services));
                    } else {
                        currentWait += waitPeriodMs;
                        this.wait(waitPeriodMs); // wait 500ms
                    }
                } catch (InterruptedException e) {

                }
            }
        }
        return true;
    }

    boolean shutdown() {
        status = Status.Stopping;
        spin.set(false);
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // pool didn't terminate after the first try
                pool.shutdownNow();
            }


            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // pool didn't terminate after the second try
            }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        status = Status.Stopped;
        return true;
    }

    public Status getStatus() {
        return status;
    }
}
