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
        //// Clearnet Sensor ////
        // for local IPFS node calls
        config.setProperty("1m5.sensors.clearnet.http.client", "true");
        // for handling mobile calls to this gateway
        config.setProperty("1m5.sensors.clearnet.http.server","true");
        config.setProperty("1m5.sensors.clearnet.http.server.ip", "localhost");
        config.setProperty("1m5.sensors.clearnet.http.server.port", "8080");
        config.setProperty("1m5.sensors.clearnet.http.server.path", "/mj82jg857ky45oj8xykfj92y78958n72z9gx57yg2");
        OneMFiveAppContext context = OneMFiveAppContext.getInstance(config);
        ClientAppManager manager = context.getClientAppManager();
        Client c = manager.getClient(true);
        DID did = new DID();
        did.setAlias("Alice");
        did.setPassphrase("1234");
        // wait for startup
        waitABit(2 * 1000);

//        testViewFile(c);
//        testMakeDirectory(c);
//        testMakeFile(c);

        waitABit(10000 * 1000);
        manager.stop();
    }

    private void testViewFile(Client c) {
        // https://ipfs.io/ipfs/QmTDMoVqvyBkNMRhzvukTDznntByUNDwyNdSfV8dZ3VKRC/readme.md
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                IPFSResponse response = (IPFSResponse)DLC.getData(IPFSResponse.class, envelope);
                if(response != null && response.resultBytes != null && response.resultBytes.length > 0) {
                    System.out.println(new String(response.resultBytes));
                }
            }
        };
        Envelope e = Envelope.documentFactory();
        IPFSRequest request = new IPFSRequest();
        request.hash = Multihash.fromBase58("QmTDMoVqvyBkNMRhzvukTDznntByUNDwyNdSfV8dZ3VKRC");
        request.file = new ByteArrayWrapper("readme.md");
        DLC.addData(IPFSRequest.class, request, e);
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_GATEWAY_GET, e);
        c.request(e, cb);
    }

    private void testMakeDirectory(Client c) {
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                IPFSResponse response = (IPFSResponse)DLC.getData(IPFSResponse.class, envelope);
                if(response != null && response.merkleNodes != null && response.merkleNodes.size() > 0) {
                    System.out.println(response.merkleNodes.get(0).hash.toString());
                }
            }
        };
        Envelope e = Envelope.documentFactory();
        IPFSRequest request = new IPFSRequest();
//        request.hash = Multihash.fromBase58("QmTDMoVqvyBkNMRhzvukTDznntByUNDwyNdSfV8dZ3VKRC");
        request.file = new ByteArrayWrapper("Syria");

        DLC.addData(IPFSRequest.class, request, e);
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_GATEWAY_ADD, e);
        c.request(e, cb);
    }

    private void testMakeFile(Client c) {
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                IPFSResponse response = (IPFSResponse)DLC.getData(IPFSResponse.class, envelope);
                if(response != null && response.merkleNodes != null && response.merkleNodes.size() > 0) {
                    System.out.println(response.merkleNodes.get(0).hash.toString());
                }
            }
        };
        Envelope e = Envelope.documentFactory();
        IPFSRequest request = new IPFSRequest();
        request.hash = Multihash.fromBase58("QmTDMoVqvyBkNMRhzvukTDznntByUNDwyNdSfV8dZ3VKRC");
        request.file = new ByteArrayWrapper("TestFileData".getBytes());

        DLC.addData(IPFSRequest.class, request, e);
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_GATEWAY_ADD, e);
        c.request(e, cb);
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
