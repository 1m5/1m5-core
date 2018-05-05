package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.orchestration.routes.SimpleRoute;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.health.HealthRecord;

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
            reply(envelope);
        } else if(OPERATION_SAVE.equals(route.getOperation())) {
            save(envelope);
            reply(envelope);
        } else {
            deadLetter(envelope);
        }
    }

    private void load(Envelope e) {
        List<Map<String,Object>> maps = ((DocumentMessage)e.getMessage()).data;
        Object entity;
        for(Map<String,Object> map : maps) {
            entity = map.get(ENTITY);
            if(entity != null && entity instanceof HealthRecord) {
                map.put(ENTITY, infoVault.getHealthDAO().loadHealthRecord(((HealthRecord)entity).getDid()));
            }
        }
    }

    private void save(Envelope e) {
        List<Map<String,Object>> maps = ((DocumentMessage)e.getMessage()).data;
        Object entity;
        for(Map<String,Object> map : maps) {
            entity = map.get(ENTITY);
            if(entity != null && entity instanceof HealthRecord) {
                infoVault.getHealthDAO().saveHealthRecord((HealthRecord)entity);
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
