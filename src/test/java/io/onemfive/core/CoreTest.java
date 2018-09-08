package io.onemfive.core;

import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.data.*;
import io.onemfive.data.util.ByteArrayWrapper;
import io.onemfive.data.util.DLC;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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
//    public void testDIDCreate() {
//        DID did = null;
//        try {
//            did = DID.create("Alice", "1234", DID.MESSAGE_DIGEST_SHA512);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            assert false;
//        }
//        ServiceCallback cb = new ServiceCallback() {
//            @Override
//            public void reply(Envelope envelope) {
//                DID did = envelope.getDID();
//                assert(did.getStatus() == DID.Status.ACTIVE);
//                lock.countDown();
//            }
//        };
//        Envelope e = Envelope.headersOnlyFactory();
//        e.setDID(did);
//        DLC.addRoute(DIDService.class, DIDService.OPERATION_CREATE, e);
//        client.request(e, cb);
//    }

//    @Test
//    public void testDIDAuthN() {
//        DID did = null;
//        try {
//            did = DID.create("Alice", "1234", DID.MESSAGE_DIGEST_SHA512);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            assert false;
//        }
//        ServiceCallback cb = new ServiceCallback() {
//            @Override
//            public void reply(Envelope envelope) {
//                DID did = envelope.getDID();
//                assert(did != null && did.getAuthenticated());
//                lock.countDown();
//            }
//        };
//        Envelope e = Envelope.headersOnlyFactory();
//        e.setDID(did);
//        DLC.addRoute(DIDService.class, DIDService.OPERATION_AUTHENTICATE,e);
//        client.request(e, cb);
//    }

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
//    public void testInfoVault() {
//        System.out.println("Starting InfoVaultDB test...");
//        InfoVaultDB infoVaultDB = InfoVaultDB.getInstance();
//        infoVaultDB.init(null);
//        DID did = infoVaultDB.getDidDAO().load("Alice");
//        List<MemoryTest> tests = infoVaultDB.getMemoryTestDAO().loadListByDID(did.getId(), 0, 10);
//        for(MemoryTest t : tests) {
//            System.out.println("MemoryTest: name="+t.getName()
//                    +", ended="+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(t.getTimeEnded())
//                    +", bac="+t.getBloodAlcoholContent()
//                    +", difficulty="+t.getDifficulty());
//        }
//        infoVaultDB.teardown();
//        System.out.println("InfoVaultDB test finished.");
//    }

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
