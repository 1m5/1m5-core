package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.infovault.nitrite.NitriteDB;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.Properties;

/**
 * InfoVault Service - Stores personal information securely while allowing access to 3rd parties with personal approval.
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    private Properties props;
    private NitriteDB db;

    public InfoVaultService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        Route route = (Route) envelope.getHeader(Envelope.ROUTE);
        switch(route.getOperation()) {
            case "Load": {
                load(envelope);
                reply(envelope);
                break;
            }
            case "Save": {
                save(envelope);
                break;
            }
            default: deadLetter(envelope); // Operation not supported
        }
    }

    private void load(Envelope envelope) {
        System.out.println(InfoVaultService.class.getSimpleName()+": Received load request.");
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        String type = (String)m.data.get("type");
        if("List".equals(type)) {

        } else {
            Long id = (Long) m.data.get("_id");
            if (type != null && id != null) {
                m.data = db.load(type, id);
            }
        }
        System.out.println(InfoVaultService.class.getSimpleName()+": Load performed.");
    }

    private void save(Envelope envelope) {
        System.out.println(InfoVaultService.class.getSimpleName()+": Received save request.");
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        String type = (String)m.data.get("type");
        if("List".equals(type)) {

        } else {
            Long id = (Long) m.data.get("_id");
            if (type != null && id != null) {
                db.save(type, m.data);
            }
        }
        System.out.println(InfoVaultService.class.getSimpleName()+": Save performed.");
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("InfoVaultService starting up...");
        try {
            props = Config.loadFromClasspath("infovault.config", properties);
            db = new NitriteDB();
            db.start(properties);
        } catch (Exception e) {
            System.out.println("InfoVaultService failed to start: "+e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        System.out.println("InfoVaultService started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        System.out.println("InfoVaultService shutting down...");
        boolean shutdown = db.shutdown();
        System.out.println("InfoVaultService shutdown.");
        return shutdown;
    }
}
