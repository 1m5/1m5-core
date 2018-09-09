package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.client.ClientStatusListener;
import io.onemfive.data.*;

import java.util.Properties;
import java.util.logging.Logger;

/**
 *  This is the class called by the 1m5.sh script on linux.
 *
 *  Not recommended for embedded use, get your own context instance and client.
 *
 * @author objectorange
 */
public class OneMFiveStandaloneLauncher {

    private static final Logger LOG = Logger.getLogger(OneMFiveStandaloneLauncher.class.getName());

    private static OneMFiveStandaloneLauncher launcher;

    private ClientAppManager.Status status;

    public static void main(String args[]) {
        LOG.info("Starting 1M5 Standalone...");

        launcher = new OneMFiveStandaloneLauncher();
        launcher.launch(args);

        LOG.info("1M5 Standalone exiting...");
        System.exit(0);
    }

    private void launch(String args[]) {
        Properties config = new Properties();
        String[] parts;
        for(String arg : args) {
            parts = arg.split("=");
            config.setProperty(parts[0],parts[1]);
        }
        try {
            config = Config.loadFromClasspath("1m5.config", config, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        OneMFiveAppContext context = OneMFiveAppContext.getInstance(config);
        ClientAppManager manager = context.getClientAppManager();
        final Client c = manager.getClient(true);

        ClientStatusListener clientStatusListener = new ClientStatusListener() {
            @Override
            public void clientStatusChanged(ClientAppManager.Status clientStatus) {
                status = clientStatus;
                LOG.info("Client Status changed: "+clientStatus.name());
                switch(status) {
                    case INITIALIZING: {
                        LOG.info("Bus reports initializing...");
                        break;
                    }
                    case READY: {
                        LOG.info("Bus reports ready.");
                        break;
                    }
                    case STOPPING: {
                        LOG.info("Bus reports stopping...");
                        break;
                    }
                    case STOPPED: {
                        LOG.info("Bus reports it stopped.");
                        break;
                    }
                }
            }
        };
        c.registerClientStatusListener(clientStatusListener);
        while(status != ClientAppManager.Status.STOPPED) {
            try {
                synchronized (launcher) {
                    launcher.wait(2 * 1000);
                }
            } catch (InterruptedException e) {

            }
        }
    }
}
