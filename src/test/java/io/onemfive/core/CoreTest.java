package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.did.DIDService;
import io.onemfive.core.infovault.InfoVault;
import io.onemfive.core.ipfs.IPFSRequest;
import io.onemfive.core.ipfs.IPFSResponse;
import io.onemfive.core.ipfs.IPFSService;
import io.onemfive.data.*;
import io.onemfive.data.health.mental.memory.MemoryTest;
import io.onemfive.data.util.ByteArrayWrapper;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.FileWrapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class CoreTest {

    private static OneMFiveAppContext oneMFiveAppContext;
    private static ClientAppManager clientAppManager;
    private static Client client;

    private static CountDownLatch lock;

    @BeforeClass
    public static void startUp() {
        oneMFiveAppContext = OneMFiveAppContext.getInstance();
        clientAppManager = oneMFiveAppContext.getClientAppManager();
        client = clientAppManager.getClient(true);
        // NOTE: Don't forget to increase latch number for each asynchronous assertion
        lock = new CountDownLatch(1);
        // Allow startup
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {

        }
    }

    public void testBus() {

    }

    public void testOrchestration() {

    }

//    @Test
    public void testIPFSGatewayListService() {
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                IPFSResponse response = (IPFSResponse)DLC.getData(IPFSResponse.class, envelope);
                assert(response != null && response.gateways != null && response.gateways.size() > 0);
                for(String gateway : response.gateways.keySet()) {
                    System.out.println("Gateways:");
                    System.out.println(gateway +":"+response.gateways.get(gateway));
                }
                lock.countDown();
            }
        };
        Envelope e = Envelope.documentFactory();
        assert(DLC.addData(IPFSRequest.class, new IPFSRequest(), e));
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_GATEWAY_LIST, e);
        client.request(e, cb);
    }

//    @Test
    public void testIPFSGatewayPublishServiceDirectory() {
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                IPFSResponse response = (IPFSResponse)DLC.getData(IPFSResponse.class, envelope);
                if(response != null && response.merkleNodes != null && response.merkleNodes.size() > 0) {
                    System.out.println(response.merkleNodes.get(0).hash.toString());
                }
                lock.countDown();
            }
        };
        // Test Directory Persisting
        Envelope e = Envelope.documentFactory();
        IPFSRequest ipfsRequest = new IPFSRequest();
        ipfsRequest.file = new ByteArrayWrapper("TestDirectory");
        DLC.addData(IPFSRequest.class, ipfsRequest, e);
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_GATEWAY_ADD, e);
        client.request(e, cb);
    }

//    @Test
    public void testIPFSGatewayPublishServiceFile() {
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                IPFSResponse response = (IPFSResponse)DLC.getData(IPFSResponse.class, envelope);
                assert(response != null && response.merkleNodes != null && response.merkleNodes.size() > 0);
                lock.countDown();
            }
        };
        // Test File Persisting
        Envelope e = Envelope.documentFactory();
        IPFSRequest ipfsRequest = new IPFSRequest();
        ipfsRequest.file = new ByteArrayWrapper("TestFile","Hello World!".getBytes());
        DLC.addData(IPFSRequest.class, ipfsRequest, e);
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_GATEWAY_ADD, e);
        client.request(e, cb);
    }

//    @Test
    public void testDIDCreate() {
        DID did = new DID();
        did.setAlias("Alice");
        did.setPassphrase("1234");
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                DID did = envelope.getDID();
                assert(did.getStatus() == DID.Status.ACTIVE);
                lock.countDown();
            }
        };
        Envelope e = Envelope.messageFactory(Envelope.MessageType.NONE);
        e.setDID(did);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_CREATE, e);
        client.request(e, cb);
    }

//    @Test
    public void testDIDAuthN() {
        DID did = new DID();
        did.setAlias("Alice");
        did.setPassphrase("1234");
        ServiceCallback cb = new ServiceCallback() {
            @Override
            public void reply(Envelope envelope) {
                DID did = envelope.getDID();
                assert(did != null && did.getAuthenticated());
                lock.countDown();
            }
        };
        Envelope e = Envelope.messageFactory(Envelope.MessageType.NONE);
        e.setDID(did);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_AUTHENTICATE,e);
        client.request(e, cb);
    }

    public void testAten() {

    }

    public void testConsensus() {

    }

    public void testContent() {

    }

    public void testContract() {

    }

    public void testDEX() {

    }

//    @Test
    public void testInfoVault() {
        System.out.println("Starting InfoVault test...");
        InfoVault infoVault = InfoVault.getInstance();
        infoVault.start(null);
        DID did = infoVault.getDidDAO().load("Alice");
        List<MemoryTest> tests = infoVault.getMemoryTestDAO().loadListByDID(did.getId(), 0, 10);
        for(MemoryTest t : tests) {
            System.out.println("MemoryTest: name="+t.getName()
                    +", ended="+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(t.getTimeEnded())
                    +", bac="+t.getBloodAlcoholContent()
                    +", difficulty="+t.getDifficulty());
        }
        infoVault.shutdown();
        System.out.println("InfoVault test finished.");
    }

    public void testKeyRing() {

    }

    public void testPrana() {

    }

    public void testRepository() {

    }

    public void testSensors() {

    }

    public void testUtil() {

    }

    @AfterClass
    public static void tearDown() {
        try {
            lock.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}
        clientAppManager.unregister(client);
    }

}
