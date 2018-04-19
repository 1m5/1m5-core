package io.onemfive.core.sensors.i2p;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.util.AppThread;
import io.onemfive.core.sensors.Sensor;
import net.i2p.router.Router;

import java.io.File;
import java.util.Properties;

/**
 * Embeds
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
    public boolean start(Properties properties) {
        System.out.println("Starting I2PSensor...");
        if(router == null) {
            System.out.println("Instantiating I2P Router...");
            File baseDir = OneMFiveAppContext.getInstance().getBaseDir();
            String baseDirPath = baseDir.getAbsolutePath();
            System.setProperty("i2p.dir.base", baseDirPath);
            System.setProperty("i2p.dir.config", baseDirPath);
            System.setProperty("wrapper.logfile", baseDirPath + "/wrapper.log");
            status = Status.INIT;
            router = new Router(properties);
        }
        if(!router.isAlive()) {
            System.out.println("Starting I2P Router...");
            router.setKillVMOnEnd(false);
            status = Status.STARTING;
            router.runRouter();
            status = Status.RUNNING;
        }
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
