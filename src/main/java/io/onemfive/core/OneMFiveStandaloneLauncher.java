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

    private boolean awaitingKey = false;
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
        Properties config = new Properties();
        OneMFiveAppContext context = OneMFiveAppContext.getInstance(config);
        ClientAppManager manager = context.getClientAppManager();
        final Client c = manager.getClient(true);

        fromDID = new DID();
        fromDID.addAlias("Alice");

        toDID = new DID();
        toDID.addAlias("Alice");

        ServiceCallback getKeyCB = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {

            }
        };



        // Step 3: Get Email
        ServiceCallback getEmailCB = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {

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

        while(status != ClientAppManager.Status.STOPPED) {
            if(status == ClientAppManager.Status.READY) {
                if(fromDID.getEncodedKey() == null) {
                    if(!awaitingKey) {
                        // Step 1: Send Key Request
                        Envelope e = Envelope.documentFactory();
                        e.setSensitivity(Envelope.Sensitivity.VERYHIGH);
                        e.setDID(fromDID);
                        DLC.addRoute(SensorsService.class, SensorsService.OPERATION_GET_KEYS,e);
                        awaitingKey = true;
                    }
                } else if(toDID.getEncodedKey() == null) {
                    toDID.addEncodedKey(fromDID.getEncodedKey());
                } else if(!emailSent) {
                    // Step 2: Send Email

                    emailSent = true;
                } else {
                    // Step 3: Awaiting Email

                }
            }
            try {
                launcher.wait(2 * 1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
