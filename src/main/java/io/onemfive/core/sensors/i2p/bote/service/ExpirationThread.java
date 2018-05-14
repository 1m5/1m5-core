package io.onemfive.core.sensors.i2p.bote.service;

import io.onemfive.core.sensors.i2p.bote.folder.ExpirationListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.i2p.util.I2PAppThread;
import net.i2p.util.Log;

public class ExpirationThread extends I2PAppThread {
    private Log log = new Log(ExpirationThread.class);
    private List<ExpirationListener> expirationListeners;

    public ExpirationThread() {
        super("ExpiratnThrd");
        setPriority(MIN_PRIORITY);
        expirationListeners = Collections.synchronizedList(new ArrayList<ExpirationListener>());
    }

    public void addExpirationListener(ExpirationListener listener) {
        expirationListeners.add(listener);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                for (ExpirationListener listener: expirationListeners)
                    listener.deleteExpired();
                TimeUnit.DAYS.sleep(1);
            } catch (InterruptedException e) {
                break;
            } catch (RuntimeException e) {   // catch unexpected exceptions to keep the thread running
                log.error("Exception caught in ExpirationThread loop", e);
            }
        }

        log.debug("ExpirationThread interrupted, thread exiting.");
    }
}
