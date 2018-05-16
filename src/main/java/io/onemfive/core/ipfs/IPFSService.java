package io.onemfive.core.ipfs;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Abstracts IPFS as an internal Service.
 *
 * @author objectorange
 */
public class IPFSService extends BaseService {

    public static final String OPERATION_LOAD = "LOAD";
    public static final String DATA_HASH = "HASH";
    public static final String DATA_SNAPSHOT = "SNAPSHOT";

    private static final String TOR_GATEWAYS_PROP = "1m5.ipfs.gateways.tor";

    private Properties config;
    private IPFS ipfs;

    private enum GatewayStatus {Active, Inactive}
    private Map<String,GatewayStatus> torGateways = new HashMap<>();

    public IPFSService() {
        super();
    }

    public IPFSService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        // Request for IPFS Service
        Route route = (Route)envelope.getHeader(Envelope.ROUTE);
        switch(route.getOperation()){
            case OPERATION_LOAD: {loadContent(envelope);reply(envelope);break;}
            default: deadLetter(envelope);
        }
    }

    private void loadContent(Envelope envelope) {
        String hash = (String)((DocumentMessage)envelope.getMessage()).data.get(0).get(DATA_HASH);
        Boolean snapshot = (Boolean)((DocumentMessage)envelope.getMessage()).data.get(0).get(DATA_SNAPSHOT);
        String torGateway = getActiveTorGateway();
        try {
            URL url;
            if(snapshot != null && snapshot) {
                url = new URL("https",torGateway,443,"/ipfs/"+hash);
            } else {
                url = new URL("https",torGateway,443,"/ipfn/"+hash);
            }
            envelope.setHeader(Envelope.URL,url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private String getActiveTorGateway() {
        for(String torGateway : torGateways.keySet()) {
            if(torGateways.get(torGateway) == GatewayStatus.Active) {
                return torGateway;
            }
        }
        return null;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(IPFSService.class.getSimpleName()+": starting...");
        try {
            config = Config.loadFromClasspath("ipfs.config", properties);
            String torGatewaysString = config.getProperty(TOR_GATEWAYS_PROP);
            if(torGatewaysString != null) {
                List<String> torGateways = Arrays.asList(torGatewaysString.split(","));
                for(String torGateway : torGateways) {
                    this.torGateways.put(torGateway, GatewayStatus.Active);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        ipfs = new IPFS();
        System.out.println(IPFSService.class.getSimpleName()+": started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        System.out.println(IPFSService.class.getSimpleName()+": stopping...");
        ipfs = null;
        System.out.println(IPFSService.class.getSimpleName()+": stopped.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
