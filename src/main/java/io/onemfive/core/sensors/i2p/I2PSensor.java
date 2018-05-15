package io.onemfive.core.sensors.i2p;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.util.AppThread;
import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.Envelope;
import net.i2p.router.Router;

import java.io.File;
import java.util.Properties;

/**
 * Embeds I2P
 *
 * @author objectorange
 */
public class I2PSensor implements Sensor {

    public enum Status {
        // These states persist even if it died.
        INIT, WAITING, STARTING, RUNNING, ACTIVE,
        // button, don't kill service when paused, stay in PAUSED
        PAUSING, PAUSED,
        //
        UNPAUSING,
        // button, kill service when stopped
        STOPPING, STOPPED,
        // Stopped by listener (no network), next: WAITING (spin waiting for network)
        NETWORK_STOPPING, NETWORK_STOPPED,
        // button,
        GRACEFUL_SHUTDOWN
    }

    private static Router router;
    private Status status = Status.INIT;
    private AppThread starterThread;

    @Override
    public boolean send(Envelope envelope) {
        return false;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(I2PSensor.class.getSimpleName()+": starting...");
        status = Status.STARTING;
        router = I2PRouterUtil.getGlobalI2PRouter(properties, true);
        status = Status.RUNNING;
        System.out.println(I2PSensor.class.getSimpleName()+": started.");
        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean gracefulShutdown() {
        // will shutdown in 11 minutes or less
        router.shutdownGracefully();
        return true;
    }
}
