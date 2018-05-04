package io.onemfive.core.infovault;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.*;

import java.util.Properties;

/**
 * InfoVault Service
 *
 * Stores personal information securely while allowing access
 * by other parties with personal approval.
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    public static final String OPERATION_LOAD = "Load";
    public static final String OPERATION_SAVE = "Save";

    private Properties props;
    private NitriteDBManager db;

    private DIDDAO diddao;

    public InfoVaultService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        Route route = (Route) envelope.getHeader(Envelope.ROUTE);
        switch(route.getOperation()) {
            case OPERATION_LOAD: {
                load(envelope);
                reply(envelope);
                break;
            }
            case OPERATION_SAVE: {
                save(envelope);
                break;
            }
            default: deadLetter(envelope); // Operation not supported
        }
    }

    private void load(Envelope envelope) {
        System.out.println(InfoVaultService.class.getSimpleName()+": Received load request.");
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        DID did = (DID)envelope.getHeader(Envelope.DID);
        String type = (String)envelope.getHeader(Envelope.DATA_TYPE);
//        Boolean isList = (Boolean)envelope.getHeader(Envelope.DATA_IS_LIST);
        if(type != null) {
            if(type.equals(DID.class.getName())) {
                m.data.get(0).put(type, diddao.load(did.getAlias()));
                System.out.println(InfoVaultService.class.getSimpleName()+".load: DID loaded.");
            } else {
                System.out.println(InfoVaultService.class.getSimpleName()+".load: Error: Loading of Type not supported yet:"+type);
                return;
            }
        } else {
            System.out.println(InfoVaultService.class.getSimpleName()+".load: Error: Not type in header provided.");
            return;
        }
        System.out.println(InfoVaultService.class.getSimpleName()+": Load performed.");
    }

    private void save(Envelope envelope) {
        System.out.println(InfoVaultService.class.getSimpleName()+": Received save request.");
        DocumentMessage m = (DocumentMessage)envelope.getMessage();
        DID did = (DID)envelope.getHeader(Envelope.DID);
        String type = (String)envelope.getHeader(Envelope.DATA_TYPE);
//        Boolean isList = (Boolean)envelope.getHeader(Envelope.DATA_IS_LIST);
        if(type != null) {
            if(type.equals(DID.class.getName())) {
                if(did.getId() == null)
                    m.data.get(0).put(type, diddao.createDID(did.getAlias(), did.getPassphrase()));
                else {
                    diddao.updateDID(did);
                    m.data.get(0).put(type, did);
                }
            } else {
                System.out.println(InfoVaultService.class.getSimpleName()+".load: Error: Loading of Type not supported yet:"+type);
            }
        } else {
            System.out.println(InfoVaultService.class.getSimpleName()+".save: Error: Not type in header provided.");
        }
        System.out.println(InfoVaultService.class.getSimpleName()+": Save performed.");
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("InfoVaultService starting up...");
        try {
            props = Config.loadFromClasspath("infovault.config", properties);
            db = new NitriteDBManager();
            db.start(properties);
            diddao = new DIDDAO(db);
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
