package io.onemfive.core.bus;

import io.onemfive.core.client.ClientAppManager;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class WorkerThreadFactory implements ThreadFactory {

    private final ClientAppManager clientAppManager;
    private final MessageChannel channel;
    private final Map<String, BaseService> services;
    private int index = 0;

    public WorkerThreadFactory(ClientAppManager clientAppManager, MessageChannel channel, Map<String, BaseService> services) {
        System.out.println("New WorkerThreadFactory");
        this.clientAppManager = clientAppManager;
        this.channel = channel;
        this.services = services;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        String threadName = "FactoryThread-"+(++index);
        System.out.println("New WorkerThread: "+threadName);
        return new WorkerThread(threadName,channel, clientAppManager, services);
    }

}
