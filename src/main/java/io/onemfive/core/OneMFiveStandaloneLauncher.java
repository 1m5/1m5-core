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

    private boolean requestedKey = false;
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
            public void reply(Envelope e) {
                DID did = e.getDID();
                if(did != null && did.getEncodedKey() != null) {
                    fromDID.addEncodedKey(did.getEncodedKey());
                    toDID.addEncodedKey(did.getEncodedKey());
                    LOG.info("Received encoded key: "+did.getEncodedKey());
                } else {
                    LOG.warning("Did not receive encoded key.");
                }
            }
        };

        ServiceCallback getEmailCB = new ServiceCallback() {
            @Override
            public void reply(Envelope e) {
                Email em = (Email)DLC.getData(Email.class,e);
                if(em != null) {
                    emailReceived = em;
                    LOG.info("Received email with subject: "+em.getSubject()+" and flag: "+em.getFlag());
                }
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
                    if(!requestedKey) {
                        // Step 1: Send Key Request
                        Envelope e = Envelope.documentFactory();
                        e.setSensitivity(Envelope.Sensitivity.VERYHIGH);
                        e.setDID(fromDID);
                        DLC.addRoute(SensorsService.class, SensorsService.OPERATION_GET_KEYS,e);
                        c.request(e,getKeyCB);
                        requestedKey = true;
                    }
                } else if(toDID.getEncodedKey() == null) {
                    toDID.addEncodedKey(fromDID.getEncodedKey());
                } else if(!emailSent) {
                    // Step 2: Send Email
                    Envelope e = Envelope.documentFactory();
                    e.setSensitivity(Envelope.Sensitivity.VERYHIGH);
                    e.setDID(fromDID);
                    Email email = new Email(toDID, fromDID, "A New Syrian Peace Deal","Today marks the 3rd attempt at a new deal for peace in the Syrian conflict.");
                    emailToSend = email;
                    DLC.addData(Email.class, email, e);
                    DLC.addRoute(SensorsService.class, SensorsService.OPERATION_SEND,e);
                    c.request(e,getEmailCB);
                    emailSent = true;
                } else {
                    // Step 3: Awaiting Email
                    if(emailReceived != null) {
                        LOG.info("Email received.");
                    } else {
                        LOG.info("Awaiting email...");
                    }
                }
            }
            try {
                launcher.wait(2 * 1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
