package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.*;
import io.onemfive.data.health.HealthRecord;
import io.onemfive.data.health.mental.memory.MemoryTest;
import io.onemfive.data.health.mental.memory.MemoryTestPopScores;
import io.onemfive.data.util.DLC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

    public InfoVaultService(MessageProducer producer) {
        super(producer);
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
                    if (test.getPopScores() == null) {
                        // New Test -> Load scores
                        double borderline = infoVault.getMemoryTestDAO().minBorderlineImpairedScore(test.getDifficulty());
                        double impaired = infoVault.getMemoryTestDAO().minImpairedScore(test.getDifficulty());
                        double gross = infoVault.getMemoryTestDAO().minGrosslyImpairedScore(test.getDifficulty());
                        MemoryTestPopScores popScores = new MemoryTestPopScores(borderline, impaired, gross);
                        test.setPopScores(popScores);
                    }
                } else {
                    DLC.addEntity(infoVault.getMemoryTestDAO().loadListByDID(e.getDID().getId(), 0, 10), e);
                }
            }
        }
    }

    private void save(Envelope e) {
        List<Map<String,Object>> maps = ((DocumentMessage)e.getMessage()).data;
        Object entity;
        for(Map<String,Object> map : maps) {
            entity = map.get(DLC.ENTITY);
            if(entity != null) {
                if(entity instanceof HealthRecord) {
                    infoVault.getHealthDAO().saveHealthRecord((HealthRecord) entity);
                } else if(entity instanceof MemoryTest) {
                    MemoryTest test = (MemoryTest)entity;
                    infoVault.getMemoryTestDAO().create(test);
                    map.put(DLC.ENTITY, test);
                }
            }
        }
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        infoVault.start(properties);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        infoVault.shutdown();
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
