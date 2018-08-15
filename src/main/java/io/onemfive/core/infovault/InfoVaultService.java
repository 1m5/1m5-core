package io.onemfive.core.infovault;

import io.onemfive.core.*;
import io.onemfive.data.*;
import io.onemfive.data.health.HealthRecord;
import io.onemfive.data.health.mental.memory.MemoryTest;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.logging.Logger;

/**
 * Asynchronous access to persistence.
 * Access to the instance of InfoVault is provided in each Service too (by BaseService) for synchronous access.
 * Developer's choice to which to use on a per-case basis by Services extending BaseService.
 * Clients always use this as they do not have direct access to InfoVault.
 * Consider using this service for heavier higher-latency work by Services extending BaseService vs using their
 * synchronous access.`
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    private static final Logger LOG = Logger.getLogger(InfoVaultService.class.getName());

    public static final String OPERATION_LOAD = "LOAD";
    public static final String OPERATION_SAVE = "SAVE";

    public InfoVaultService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        switch(r.getOperation()) {
            case OPERATION_LOAD: {load(e);break;}
            case OPERATION_SAVE: {save(e);break;}
            default: deadLetter(e);
        }
    }

    private void load(Envelope e) {
        Object entity = DLC.getEntity(e);
        List list = null;
        if(entity != null) {
            if(entity instanceof List) {
                list = (List)entity;
                entity = list.get(0);
            }
            if(entity instanceof HealthRecord) {
                if(list == null) {
                    DLC.addEntity(infoVault.getHealthDAO().loadHealthRecord(((HealthRecord) entity).getDid()),e);
                } else {
                    LOG.warning("HealthRecord lists not supported for loading yet.");
                }
            } else if(entity instanceof MemoryTest) {
                if(list == null) {
                    MemoryTest test = (MemoryTest) entity;
                    DLC.addEntity(infoVault.getMemoryTestDAO().load(test.getId()),e);
                } else {
                    int offset = Integer.parseInt((String)DLC.getValue("offset",e));
                    int max = Integer.parseInt((String)DLC.getValue("max",e));
                    if(max == 0) max = 10; // default
                    if(max > 0)
                        DLC.addEntity(infoVault.getMemoryTestDAO().loadListByDID(e.getDID().getId(), offset, max), e);
                    else
                        DLC.addEntity(infoVault.getMemoryTestDAO().loadListByDID(e.getDID().getId()), e);

                }
            }
        }
    }

    private void save(Envelope e) {
        Object entity = DLC.getEntity(e);
        if(entity != null) {
            if(entity instanceof HealthRecord) {
                infoVault.getHealthDAO().saveHealthRecord((HealthRecord) entity);
            } else if(entity instanceof MemoryTest) {
                LOG.info("Saving MemoryTest...");
                MemoryTest test = (MemoryTest)entity;
                infoVault.getMemoryTestDAO().create(test);
                DLC.addEntity(test,e);
                LOG.info("MemoryTest saved.");
            } else if(entity instanceof List) {
                List l = (List)entity;
                if(l.size() > 0) {
                    if(l.get(0) instanceof MemoryTest) {
                        Iterator i = l.iterator();
                        MemoryTest test;
                        while(i.hasNext()) {
                            test = (MemoryTest)i.next();
                            infoVault.getMemoryTestDAO().create(test);
                        }
                    }
                }
            }
        }
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

//    public static void main(String[] args) {
//        // kick start things
//        OneMFiveAppContext ctx = OneMFiveAppContext.getInstance();
//        InfoVaultService s = new InfoVaultService(null, null);
//        s.start(null);
//        DID did = new DID();
//        did.setId(1L);
//        MemoryTest t = MemoryTest.newInstance("test",did.getId());
//        t.setBaseline(false);
//        t.setTimeStarted(new Date(new Date().getTime() - (5 * 60 * 1000)));
//        t.setTimeEnded(new Date());
//        t.setDifficulty(1);
//        t.addSuccess(1000);
//        t.addMiss(5000);
//        t.addNegative(3000);
//        t.addInappropriate(500);
//        t.setBloodAlcoholContent(0.08);
//        t.setCardsUsed(Arrays.asList(1,2,3,4,5,6));
//        Envelope saveEnv = Envelope.documentFactory();
//        DLC.addEntity(t, saveEnv);
//        s.save(saveEnv);
//
//        Envelope loadEnv = Envelope.documentFactory();
//        MemoryTest t2 = new MemoryTest();
//        t2.setId(t.getId());
//        DLC.addEntity(t2, loadEnv);
//        s.load(loadEnv);
//        t2 = (MemoryTest)DLC.getEntity(loadEnv);
//        assert("test".equals(t2.getName()));
//        assert(t2.getBloodAlcoholContent() == 0.08);
//    }
}
