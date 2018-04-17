package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.lid.LIDService;
import io.onemfive.data.*;

/**
 *  This is the class called by the sc.sh script on linux
 *  and the sc.exe launcher on Windows.
 *  (i.e. no wrapper)
 *
 *  Setup of wrapper.log file is moved to WorkingDir.java
 *  Until WorkingDir is called, the existing stdout / stderr will be used.
 *
 *  Not recommended for embedded use. Instantiate OneMFiveStandaloneLauncher() and call launch() yourself.
 *
 * @author objectorange
 */
public class OneMFiveStandaloneLauncher {

    private static OneMFiveStandaloneLauncher launcher;

    public static void main(String args[]) {
        System.out.println("Starting Synaptic Celerity...");
        OneMFiveVersion.print();

        launcher = new OneMFiveStandaloneLauncher();
        launcher.launch(args);

        System.out.println("Synaptic Celerity exiting...");
        System.exit(0);
    }

    private void launch(String args[]) {
        OneMFiveAppContext context = OneMFiveAppContext.getInstance();
        ClientAppManager manager = context.getClientAppManager();
        Client c = manager.getClient(true);
        LID lid = new LID();
        lid.setAlias("Alice");
        lid.setPassphrase("1234");
        // Test
        int numMessages = 4;
        int failsafe = numMessages * 2; // prevent runaway loop
        Envelope e;
        for(long i=0; i<numMessages; i++) {
            try {
                ServiceCallback cb = new ServiceCallback() {
                    @Override
                    public void reply(Envelope envelope) {
                        String service = (String)envelope.getHeaders().get(Envelope.SERVICE);
                        String operation = (String)envelope.getHeaders().get(Envelope.OPERATION);
                        System.out.println(ServiceCallback.class.getSimpleName()+": id="+envelope.getId()+", service="+service+", operation="+operation+", message="+envelope.getMessage());
                    }
                };
                e = Envelope.documentFactory(i+1);
                e.setHeader(Envelope.SERVICE, LIDService.class.getName());
                e.setHeader(Envelope.OPERATION, "Create");
                ((DocumentMessage)e.getMessage()).data.put(LID.class.getName(),lid);
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
