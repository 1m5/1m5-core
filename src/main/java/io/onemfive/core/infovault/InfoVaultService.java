package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.*;
import io.onemfive.data.health.HealthRecord;
import io.onemfive.data.health.mental.memory.MemoryTest;
import io.onemfive.data.util.DLC;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

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
        List<Map<String,Object>> maps = ((DocumentMessage)e.getMessage()).data;
        Object entity;
        for(Map<String,Object> map : maps) {
            entity = map.get(DLC.ENTITY);
            if(entity != null) {
                if(entity instanceof HealthRecord) {
                    map.put(DLC.ENTITY, infoVault.getHealthDAO().loadHealthRecord(((HealthRecord) entity).getDid()));
                } else if(entity instanceof MemoryTest) {
                    MemoryTest test = (MemoryTest)entity;
                    if(test.getId() == null) {
                        // New Test -> Load scores
                        double borderline = infoVault.getMemoryTestDAO().minBorderlineImpairedScore(test.getDifficulty());
                        double impaired = infoVault.getMemoryTestDAO().minImpairedScore(test.getDifficulty());
                        double gross = infoVault.getMemoryTestDAO().minGrosslyImpairedScore(test.getDifficulty());
                        test.setImpairmentScores(borderline, impaired, gross);
                    }
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
        System.out.println(InfoVaultService.class.getSimpleName()+": starting...");
        infoVault.start(properties);
        System.out.println(InfoVaultService.class.getSimpleName()+": started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        System.out.println(InfoVaultService.class.getSimpleName()+": shutting down...");
        infoVault.shutdown();
        System.out.println(InfoVaultService.class.getSimpleName()+": shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
