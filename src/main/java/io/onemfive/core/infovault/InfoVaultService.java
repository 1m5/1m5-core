package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.LID;
import io.onemfive.data.HealthRecord;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * InfoVault Service - Stores personal information securely while allowing access to 3rd parties with personal approval.
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    public InfoVaultService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        String operation = (String) envelope.getHeader(Envelope.OPERATION);
        switch(operation) {
            case "Load": load(envelope);break;
            default: deadLetter(envelope); // Operation not supported
        }
    }

    private void load(Envelope envelope) {
        System.out.println(InfoVaultService.class.getSimpleName()+": Received load request.");
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        if(m != null) {
            Set<Map.Entry<String, Object>> entries = m.data.entrySet();
            for (Map.Entry entry : entries) {
                Object obj = entry.getValue();
                if (obj instanceof HealthRecord) {
                    load((HealthRecord) obj);
                }
            }
        }
    }

    private void load(HealthRecord healthRecord) {
        System.out.println(InfoVaultService.class.getSimpleName()+": Received load HealthRecord request.");
        LID lid = healthRecord.getLid();
        if(lid != null) {
            System.out.println(InfoVaultService.class.getSimpleName()+": LID provided (alias="+lid.getAlias()+"), looking up HealthRecord...");
            if ("Alice".equals(lid.getAlias()))
                healthRecord.setOverallHealth(HealthRecord.HealthStatus.Good);
            else
                healthRecord.setOverallHealth(HealthRecord.HealthStatus.Unknown);
        }
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("InfoVaultService starting up...");

        System.out.println("InfoVaultService started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        System.out.println("InfoVaultService shutting down...");

        System.out.println("InfoVaultService shutdown.");
        return true;
    }
}
