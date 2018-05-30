package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.did.DIDService;
import io.onemfive.core.ipfs.IPFSRequest;
import io.onemfive.core.ipfs.IPFSResponse;
import io.onemfive.core.ipfs.IPFSService;
import io.onemfive.data.*;
import io.onemfive.data.util.ByteArrayWrapper;
import io.onemfive.data.util.DLC;

import java.io.FileInputStream;
import java.io.IOException;
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
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                IPFSResponse response = (IPFSResponse)DLC.getData(IPFSResponse.class, envelope);
                if(response != null && response.merkleNodes != null && response.merkleNodes.size() > 0) {
                    System.out.println(response.merkleNodes.get(0).hash.toString());
                }
            }
        };
        // Test Directory Persisting
        Envelope e = Envelope.documentFactory();
        IPFSRequest ipfsRequest = new IPFSRequest();
        ipfsRequest.file = new ByteArrayWrapper("TestDirectory");
        DLC.addData(IPFSRequest.class, ipfsRequest, e);
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_GATEWAY_ADD, e);
        c.request(e, cb);
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
