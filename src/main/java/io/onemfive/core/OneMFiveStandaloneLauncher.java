package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.client.ClientStatusListener;
import io.onemfive.core.did.DIDService;
import io.onemfive.core.ipfs.IPFSRequest;
import io.onemfive.core.ipfs.IPFSResponse;
import io.onemfive.core.ipfs.IPFSService;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.*;
import io.onemfive.data.util.ByteArrayWrapper;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.Multihash;

import java.io.FileInputStream;
import java.io.IOException;
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
    private DID toDID;
    private DID fromDID;
    private Email emailToSend;
    private Email emailReceived;
    private String messageString;

    private boolean requestedKey = false;
    private boolean receivedKey = false;
    private boolean emailSubscribed = false;
    private boolean emailSent = false;

    public static void main(String args[]) {
        LOG.info("Starting 1M5 Standalone...");
        OneMFiveVersion.print();

        launcher = new OneMFiveStandaloneLauncher();
        launcher.launch(args);

        LOG.info("1M5 Standalone exiting...");
        System.exit(0);
    }

    private void launch(String args[]) {
        String baseDir = args[0];
        Properties config = new Properties();
        config.setProperty("1m5.dir.base",baseDir);
        OneMFiveAppContext context = OneMFiveAppContext.getInstance(config);
        ClientAppManager manager = context.getClientAppManager();
        final Client c = manager.getClient(true);

        fromDID = new DID();
        fromDID.setAlias("Alice");

        toDID = new DID();
        toDID.setAlias("Alice");

        Subscription subscription = new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                EventMessage m = (EventMessage)e.getMessage();
                emailReceived = (Email)m.getMessage();
                LOG.info("Received Email: id="+m.getId()+" type="+m.getType()+" name="+m.getName());
            }
        };

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
        int numberEmailsSent = 0;
        while(status != ClientAppManager.Status.STOPPED) {
            if(status == ClientAppManager.Status.READY) {
                if(!emailSubscribed) {
                    c.subscribeToEmail(subscription);
                    emailSubscribed = true;
                } else if(!emailSent) {
                    // Step 4: Send Email
                    Envelope e = Envelope.documentFactory();
                    e.setSensitivity(Envelope.Sensitivity.VERYHIGH);
                    e.setDID(fromDID);
                    messageString = ++numberEmailsSent +" attempts at a new deal for peace in the Syrian conflict.";
                    Email email = new Email(toDID, fromDID, numberEmailsSent+" Tries at Syrian Peace Deal ",messageString);
                    emailToSend = email;
                    DLC.addData(Email.class, email, e);
                    DLC.addRoute(SensorsService.class, SensorsService.OPERATION_SEND,e);
                    c.request(e);
                    emailSent = true;
                } else if(emailReceived != null) {
                    // Step 5: Awaiting Email
                    LOG.info("Email received: message="+emailReceived.getMessage());
                    assert(messageString.equals(emailReceived.getMessage()));
                    emailReceived = null;
                    emailSent = false;
                }
            }
            try {
                synchronized (launcher) {
                    launcher.wait(2 * 1000);
                }
            } catch (InterruptedException e) {

            }
        }
    }
}
