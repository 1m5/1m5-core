package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.did.DIDService;
import io.onemfive.data.*;

/**
 *  This is the class called by the 1m5.sh script on linux.
 *
 *  Not recommended for embedded use, get your own context instance and client.
 *
 * @author objectorange
 */
public class OneMFiveStandaloneLauncher {

    private static OneMFiveStandaloneLauncher launcher;

    public static void main(String args[]) {
        System.out.println("Starting 1M5 Standalone...");
        OneMFiveVersion.print();

        launcher = new OneMFiveStandaloneLauncher();
        launcher.launch(args);

        System.out.println("1M5 Standalone exiting...");
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
                    public void reply(Envelope envelope) {
                        Route route = (Route) envelope.getHeaders().get(Envelope.ROUTE);
                        System.out.println(ServiceCallback.class.getSimpleName()+": id="+envelope.getId()+", service="+route.getService()+", operation="+route.getOperation()+", message="+envelope.getMessage());
                    }
                };
                e = Envelope.messageFactory(i+1, Envelope.MessageType.NONE);
                e.setHeader(Envelope.ROUTE, new SimpleRoute(DIDService.class.getName(),"Create"));
                e.setHeader(Envelope.DID, did);
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
