package io.onemfive.core;

import io.onemfive.core.bus.ServiceBus;
import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.lid.LIDService;
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
        lock = new CountDownLatch(2);
    }

    @Test
    public void testBus() {

    }

    @Test
    public void testOrchestration() {

    }

    @Test
    public void testLIDCreate() {
        LID lid = new LID();
        lid.setAlias("Alice");
        lid.setPassphrase("1234");
        Envelope e;
        try {
            ServiceCallback cb = new ServiceCallback() {
                @Override
                public void reply(Envelope envelope) {
                    DocumentMessage message = (DocumentMessage)envelope.getMessage();
                    LID lid = (LID)message.data.get(LID.class.getName());
                    assert(lid.getStatus() == LID.Status.ACTIVE);
                    lock.countDown();
                }
            };
            e = Envelope.documentFactory(1L);
            e.setHeader(Envelope.SERVICE, LIDService.class.getName());
            e.setHeader(Envelope.OPERATION, "Create");
            ((DocumentMessage)e.getMessage()).data.put(LID.class.getName(),lid);
            client.request(e, cb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testLIDAuthN() {
        LID lid = new LID();
        lid.setAlias("Alice");
        lid.setPassphrase("1234");
        Envelope e;
        try {
            ServiceCallback cb = new ServiceCallback() {
                @Override
                public void reply(Envelope envelope) {
                    DocumentMessage message = (DocumentMessage)envelope.getMessage();
                    LID lid = (LID)message.data.get(LID.class.getName());
                    assert(lid.getAuthenticated());
                    lock.countDown();
                }
            };
            e = Envelope.documentFactory(1L);
            e.setHeader(Envelope.SERVICE, LIDService.class.getName());
            e.setHeader(Envelope.OPERATION, "Authenticate");
            ((DocumentMessage)e.getMessage()).data.put(LID.class.getName(),lid);
            client.request(e, cb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testAten() {

    }

    @Test
    public void testConsensus() {

    }

    @Test
    public void testContent() {

    }

    @Test
    public void testContract() {

    }

    @Test
    public void testDEX() {

    }

    @Test
    public void testInfoVault() {

    }

    @Test
    public void testKeyRing() {

    }

    @Test
    public void testPrana() {

    }

    @Test
    public void testRepository() {

    }

    @Test
    public void testSensors() {

    }

    @Test
    public void testUtil() {

    }

    @AfterClass
    public static void tearDown() {
        try {
            lock.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}
        clientAppManager.stop();
    }

}
