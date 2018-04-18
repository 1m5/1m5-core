package io.onemfive.core.infovault;

import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.MessageProducer;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;

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
        Set<Map.Entry<String,Object>> entries = m.data.entrySet();
        for(Map.Entry entry : entries) {

        }
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("InfoVaultService not implemented yet.");
        return true;
    }
}
