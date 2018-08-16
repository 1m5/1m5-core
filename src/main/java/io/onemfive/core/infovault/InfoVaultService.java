package io.onemfive.core.infovault;

import io.onemfive.core.*;
import io.onemfive.data.*;
import io.onemfive.data.health.HealthRecord;
import io.onemfive.data.health.mental.memory.MemoryTest;
import io.onemfive.data.util.DLC;

import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

/**
 * Asynchronous access to persistence.
 * Access to the instance of InfoVault is provided in each Service too (by BaseService) for synchronous access.
 * Developer's choice to which to use on a per-case basis by Services extending BaseService.
 * Clients always use this service as they do not have direct access to InfoVault.
 * Consider using this service for heavier higher-latency work by Services extending BaseService vs using their
 * synchronous access instance in BaseService.
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    private static final Logger LOG = Logger.getLogger(InfoVaultService.class.getName());

    public static final String OPERATION_LOAD = "LOAD";
    public static final String OPERATION_SAVE = "SAVE";

    private static SecureRandom random = new SecureRandom();

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
                    HealthRecord hr = infoVault.getHealthDAO().loadHealthRecord(((HealthRecord) entity).getDid());
                    if(hr==null) {
                        hr = new HealthRecord();
                        hr.setId(random.nextLong());
                        hr.setDid(e.getDID().getId());
                        DLC.addEntity(hr,e);
                        save(e);
                    }
                    DLC.addEntity(hr,e);
                } else {
                    LOG.warning("HealthRecord lists not supported for loading yet.");
                }
            } else if(entity instanceof MemoryTest) {
                if(list == null) {
                    MemoryTest test = (MemoryTest) entity;
                    DLC.addEntity(infoVault.getMemoryTestDAO().load(test.getId()),e);
                } else {
                    if(DLC.getValue("offset",e) != null && DLC.getValue("max",e) != null) {
                        int offset = Integer.parseInt((String) DLC.getValue("offset", e));
                        int max = Integer.parseInt((String) DLC.getValue("max", e));
                        if (max == 0)
                            max = 10; // default
                        DLC.addEntity(infoVault.getMemoryTestDAO().loadListByDID(e.getDID().getId(), offset, max), e);
                    } else
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

    public static void main(String[] args) {
        // kick start things
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
        s.save(saveEnv1);

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
        s.save(saveEnv2);

        Envelope l1 = Envelope.documentFactory();
        l1.setDID(did);
        MemoryTest lt = new MemoryTest();
        List<MemoryTest> tests = new ArrayList<>();
        tests.add(lt);
        DLC.addEntity(tests,l1);
        s.load(l1);
        List<MemoryTest> tests2 = (List<MemoryTest>)DLC.getEntity(l1);
        for(MemoryTest t : tests2) {
            LOG.info(t.toString());
        }
    }
}
