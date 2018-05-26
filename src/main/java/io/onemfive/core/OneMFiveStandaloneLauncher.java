package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.did.DIDService;
import io.onemfive.data.*;

import java.util.logging.Logger;

/**
 *  This is the class called by the 1m5.sh script on linux.
 *
 *  Not recommended for embedded use, get your own context instance and client.
 *
 * @author objectorange
 */
public class OneMFiveStandaloneLauncher {

    static {
        System.setProperty("java.util.logging.config.file","logging.config");
    }

    private static final Logger LOG = Logger.getLogger(OneMFiveStandaloneLauncher.class.getName());

    private static OneMFiveStandaloneLauncher launcher;

    public static void main(String args[]) {
        LOG.info("Starting 1M5 Standalone...");
        OneMFiveVersion.print();

        launcher = new OneMFiveStandaloneLauncher();
        launcher.launch(args);

        LOG.info("1M5 Standalone exiting...");
        System.exit(0);
    }

    private void launch(String args[]) {
        OneMFiveAppContext context = OneMFiveAppContext.getInstance();
        ClientAppManager manager = context.getClientAppManager();
        Client c = manager.getClient(true);
        DID did = new DID();
        did.setAlias("Alice");
        did.setPassphrase("1234");
        // Test
        int numMessages = 4;
        int failsafe = numMessages * 2; // prevent runaway loop
        Envelope e;
        for(long i=0; i<numMessages; i++) {
            try {
                ServiceCallback cb = new ServiceCallback() {
                    @Override
                    public void reply(Envelope e) {
                        Route route = e.getRoute();
                        LOG.info("CB: id="+e.getId()+", service="+route.getService()+", operation="+route.getOperation()+", message="+e.getMessage());
                    }
                };
                e = Envelope.messageFactory(i+1, Envelope.MessageType.NONE);
                e.getDRG().addRoute(new SimpleRoute(DIDService.class.getName(),DIDService.OPERATION_CREATE));
                e.setDID(did);
                c.request(e, cb);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if(failsafe-- == 0) break;
        }
        waitABit(10 * 1000);
        manager.stop();
    }

    private void waitABit(long waitTime) {
        synchronized (this.launcher) {
            try {
                launcher.wait(waitTime);
            } catch (InterruptedException e) {

            }
        }
    }
}
