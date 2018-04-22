package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.DID;

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
        if(m != null && m.data.containsKey("type")) {
            final String objType = (String)m.data.get("type");
            switch (objType) {
                case "HealthRecord" : {
                    loadHealthRecord(envelope);
                }
            }
        }
    }

    private void loadHealthRecord(Envelope envelope) {
        System.out.println(InfoVaultService.class.getSimpleName()+": Received load HealthRecord request.");
        DID did = (DID)envelope.getHeader(Envelope.DID);
        if(did != null) {
            DocumentMessage m = (DocumentMessage)envelope.getMessage();
            // Canned data for now
            // TODO: replace with encrypted persistence mechanism
            System.out.println(InfoVaultService.class.getSimpleName()+": DID provided (alias="+did.getAlias()+"), looking up HealthRecord...");
            if ("Alice".equals(did.getAlias())) {
                m.data.put("healthStatus","Good");
            } else {
                m.data.put("healthStatus","Unknown");
            }
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
