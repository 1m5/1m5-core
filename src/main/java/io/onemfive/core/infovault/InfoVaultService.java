package io.onemfive.core.infovault;

import io.onemfive.core.*;
import io.onemfive.data.*;
import io.onemfive.data.health.mental.memory.MemoryTest;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.logging.Logger;

/**
 * Asynchronous access to persistence.
 * Access to the instance of InfoVaultDB is provided in each Service too (by BaseService) for synchronous access.
 * Developer's choice to which to use on a per-case basis by Services extending BaseService.
 * Clients always use this service as they do not have direct access to InfoVaultDB.
 * Consider using this service for heavier higher-latency work by Services extending BaseService vs using their
 * synchronous access instance in BaseService.
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    private static final Logger LOG = Logger.getLogger(InfoVaultService.class.getName());

    public static final String OPERATION_EXECUTE = "EXECUTE";

    public InfoVaultService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        switch(r.getOperation()) {
            case OPERATION_EXECUTE: {execute(e);break;}
            default: deadLetter(e);
        }
    }

    private void execute(Envelope e) {
        DAO dao = (DAO)DLC.getData(DAO.class, e);
        dao.execute();
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

    public static void main(String[] args) {
        // kick init things
        OneMFiveAppContext ctx = OneMFiveAppContext.getInstance();
        InfoVaultService s = new InfoVaultService(null, null);
        s.start(null);

        DID did = new DID();
        did.setId(1L);

        MemoryTest t1 = MemoryTest.newInstance("test1",did.getId());
        t1.setBaseline(false);
        t1.setTimeStarted(new Date(new Date().getTime() - (60 * 60 * 1000)));
        t1.setTimeEnded(new Date(new Date().getTime() - (55 * 60 * 1000)));
        t1.setDifficulty(1);
        t1.addSuccess(1000);
        t1.addMiss(5000);
        t1.addNegative(3000);
        t1.addInappropriate(500);
        t1.setBloodAlcoholContent(0.04);
        t1.setCardsUsed(Arrays.asList(3,6,7,9,12,22));
        Envelope saveEnv1 = Envelope.documentFactory();
        saveEnv1.setDID(did);
        DLC.addEntity(t1, saveEnv1);
//        s.save(saveEnv1);

        MemoryTest t2 = MemoryTest.newInstance("test",did.getId());
        t2.setBaseline(false);
        t2.setTimeStarted(new Date(new Date().getTime() - (10 * 60 * 1000)));
        t2.setTimeEnded(new Date());
        t2.setDifficulty(1);
        t2.addSuccess(2000);
        t2.addMiss(10000);
        t2.addNegative(6000);
        t2.addInappropriate(1000);
        t2.setBloodAlcoholContent(0.08);
        t2.setCardsUsed(Arrays.asList(1,2,3,4,5,6));
        Envelope saveEnv2 = Envelope.documentFactory();
        saveEnv2.setDID(did);
        DLC.addEntity(t1, saveEnv2);
//        s.save(saveEnv2);

        Envelope l1 = Envelope.documentFactory();
        l1.setDID(did);
        MemoryTest lt = new MemoryTest();
        List<MemoryTest> tests = new ArrayList<>();
        tests.add(lt);
        DLC.addEntity(tests,l1);
//        s.load(l1);
        List<MemoryTest> tests2 = (List<MemoryTest>)DLC.getEntity(l1);
        for(MemoryTest t : tests2) {
            LOG.info(t.toString());
        }
    }
}
