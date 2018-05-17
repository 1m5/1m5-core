package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.SimpleRoute;
import io.onemfive.data.health.HealthRecord;
import io.onemfive.data.health.mental.memory.MemoryTest;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    public static String OPERATION_LOAD = "LOAD";
    public static String OPERATION_SAVE = "SAVE";

    public static String ENTITY = "ENTITY";

    public InfoVaultService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        SimpleRoute route = (SimpleRoute)envelope.getHeader(Envelope.ROUTE);
        if(OPERATION_LOAD.equals(route.getOperation())) {
            load(envelope);
        } else if(OPERATION_SAVE.equals(route.getOperation())) {
            save(envelope);
        } else {
            deadLetter(envelope);
        }
    }

    private void load(Envelope e) {
        List<Map<String,Object>> maps = ((DocumentMessage)e.getMessage()).data;
        Object entity;
        for(Map<String,Object> map : maps) {
            entity = map.get(ENTITY);
            if(entity != null) {
                if(entity instanceof HealthRecord) {
                    map.put(ENTITY, infoVault.getHealthDAO().loadHealthRecord(((HealthRecord) entity).getDid()));
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
            entity = map.get(ENTITY);
            if(entity != null) {
                if(entity instanceof HealthRecord) {
                    infoVault.getHealthDAO().saveHealthRecord((HealthRecord) entity);
                } else if(entity instanceof MemoryTest) {
                    MemoryTest test = (MemoryTest)entity;
                    infoVault.getMemoryTestDAO().create(test);
                    map.put(ENTITY, test);
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
