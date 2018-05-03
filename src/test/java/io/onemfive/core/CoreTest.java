package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.did.DIDService;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.orchestration.routes.SimpleRoute;
import io.onemfive.data.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
    }

    public void testBus() {

    }

    public void testOrchestration() {

    }

//    @Test
    public void testDIDCreate() {
        DID did = new DID();
        did.setAlias("Alice");
        did.setPassphrase("1234");
        Envelope e;
        try {
            ServiceCallback cb = new ServiceCallback() {
                @Override
                public void reply(Envelope envelope) {
                    DID did = (DID)envelope.getHeader(Envelope.DID);
                    assert(did.getStatus() == DID.Status.ACTIVE);
                    lock.countDown();
                }
            };
            e = Envelope.messageFactory(Envelope.MessageType.NONE);
            e.setHeader(Envelope.ROUTE, new SimpleRoute(DIDService.class.getName(),"Create"));
            e.setHeader(Envelope.DID, did);
            client.request(e, cb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    @Test
    public void testDIDAuthN() {
        DID did = new DID();
        did.setAlias("Alice");
        did.setPassphrase("1234");
        Envelope e;
        try {
            ServiceCallback cb = new ServiceCallback() {
                @Override
                public void reply(Envelope envelope) {
                    DID did = (DID)envelope.getHeader(Envelope.DID);
                    assert(did != null && did.getAuthenticated());
                    lock.countDown();
                }
            };
            e = Envelope.messageFactory(Envelope.MessageType.NONE);
            e.setHeader(Envelope.ROUTE, new SimpleRoute(DIDService.class.getName(),"Authenticate"));
            e.setHeader(Envelope.DID, did);
            client.request(e, cb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        DID did = new DID();
        did.setAlias("Alice");
        did.setPassphrase("1234");
        Envelope e;
        try {
            ServiceCallback cb = new ServiceCallback() {
                @Override
                public void reply(Envelope envelope) {
                    DocumentMessage m = (DocumentMessage)envelope.getMessage();
                    assert("Good".equals(m.data.get(0).get("healthStatus")));
                    lock.countDown();
                }
            };
            e = Envelope.documentFactory();
            e.setHeader(Envelope.ROUTE, new SimpleRoute(InfoVaultService.class.getName(),"Load"));
            e.setHeader(Envelope.DID, did);
            e.setHeader(Envelope.DATA_TYPE,"HealthRecord");
            client.request(e, cb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            lock.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}
        clientAppManager.unregister(client);
    }

}
