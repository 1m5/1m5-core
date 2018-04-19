package io.onemfive.core.util;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Like {@link OOMHandledThread} but with per-thread OOM listeners,
 * rather than a static router-wide listener list,
 * so that an OOM in an app won't call the sc listener
 * to shutdown the whole application.
 *
 * This is preferred for application use.
 * See {@link OOMHandledThread} for features.
 */
public class AppThread extends OOMHandledThread {

    private final Set<OOMEventListener> threadListeners = new CopyOnWriteArraySet<>();

    public AppThread() {
        super();
    }

    public AppThread(String name) {
        super(name);
    }

    public AppThread(Runnable r) {
        super(r);
    }

    public AppThread(Runnable r, String name) {
        super(r, name);
    }

    public AppThread(Runnable r, String name, boolean isDaemon) {
        super(r, name, isDaemon);
    }

    public AppThread(ThreadGroup group, Runnable r, String name) {
        super(group, r, name);
    }

    @Override
    protected void fireOOM(OutOfMemoryError oom) {
        for (OOMEventListener listener : threadListeners)
            listener.outOfMemory(oom);
    }

    /** register a new component that wants notification of OOM events */
    public void addOOMEventThreadListener(OOMEventListener lsnr) {
        threadListeners.add(lsnr);
    }

    /** unregister a component that wants notification of OOM events */
    public void removeOOMEventThreadListener(OOMEventListener lsnr) {
        threadListeners.remove(lsnr);
    }
}
