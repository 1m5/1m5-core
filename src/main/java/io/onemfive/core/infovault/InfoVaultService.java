package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.data.*;
import io.onemfive.data.health.HealthRecord;
import io.onemfive.data.health.mental.memory.MemoryTest;
import io.onemfive.data.health.mental.memory.MemoryTestPopScores;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.logging.Logger;

/**
 * TODO: Add Description
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
                MemoryTest test = (MemoryTest)entity;
                infoVault.getMemoryTestDAO().create(test);
                DLC.addEntity(test,e);
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
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);
        infoVault.start(properties);
        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);
        infoVault.shutdown();
        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
