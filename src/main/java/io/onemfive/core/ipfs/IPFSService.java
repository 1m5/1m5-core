package io.onemfive.core.ipfs;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Abstracts IPFS as an internal Service.
 *
 * @author objectorange
 */
public class IPFSService extends BaseService {

    public static final String OPERATION_PUBLISH = "PUBLISH";
    public static final String OPERATION_PUBLISH_RETURN = "PUBLISH_RETURN";
    public static final String OPERATION_LOAD = "LOAD";
    public static final String OPERATION_LOAD_RETURN = "LOAD_RETURN";

    private static final String USE_TOR_PROP = "1m5.ipfs.gateways.useTor";
    private static final String TOR_GATEWAYS_PROP = "1m5.ipfs.gateways.tor";
    private static final String CLEARNET_GATEWAYS_PROP = "1m5.ipfs.gateways.clear";

    private Properties config;
    private IPFS ipfs;
    private boolean useTor = false;

    private enum GatewayStatus {Active, Inactive}
    private Map<String,GatewayStatus> torGateways = new HashMap<>();
    private Map<String,GatewayStatus> clearnetGateways = new HashMap<>();

    public IPFSService() {
        super();
    }

    public IPFSService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope e) {
        // Request for IPFS Service
        Route route = e.getRoute();
        switch(route.getOperation()){
            case OPERATION_PUBLISH: {buildPublishRequest(e);break;}
            case OPERATION_PUBLISH_RETURN: {packContentHash(e);break;}
            case OPERATION_LOAD: {buildLoadRequest(e);break;}
            case OPERATION_LOAD_RETURN: {loadContent(e);break;}
            default: deadLetter(e);
        }
    }

    private void buildPublishRequest(Envelope e) {
        e.getDRG().addRoute(new SimpleRoute(IPFSService.class.getName(),IPFSService.OPERATION_PUBLISH_RETURN));

    }

    private void packContentHash(Envelope e) {

    }

    private void buildLoadRequest(Envelope e) {
        DocumentMessage m = ((DocumentMessage)e.getMessage());
        String hash = (String)m.data.get(0).get(DLC.HASH);
        Boolean snapshot = (Boolean)((DocumentMessage)e.getMessage()).data.get(0).get(DLC.SNAPSHOT);
        String gateway = getActiveGateway();
        try {
            URL url;
            if(snapshot != null && snapshot) {
                url = new URL("https",gateway,443,"/ipfs/"+hash);
            } else {
                url = new URL("https",gateway,443,"/ipfn/"+hash);
            }
            e.setURL(url);
            // Add additional routes backwards as it's a stack
            e.getDRG().addRoute(new SimpleRoute(IPFSService.class.getName(),IPFSService.OPERATION_LOAD_RETURN));
            e.getDRG().addRoute(new SimpleRoute(SensorsService.class.getName(),SensorsService.OPERATION_SEND));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadContent(Envelope e) {

    }

    private String getActiveGateway() {
        if(useTor) {
            for (String torGateway : torGateways.keySet()) {
                if (torGateways.get(torGateway) == GatewayStatus.Active) {
                    return torGateway;
                }
            }
        } else {
            for (String clearnetGateway : clearnetGateways.keySet()) {
                if (clearnetGateways.get(clearnetGateway) == GatewayStatus.Active) {
                    return clearnetGateway;
                }
            }
        }
        return null;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(IPFSService.class.getSimpleName()+": starting...");
        try {
            config = Config.loadFromClasspath("ipfs.config", properties);
            String useTorString = config.getProperty(USE_TOR_PROP);
            if(useTorString != null) {
                this.useTor = Boolean.getBoolean(useTorString);
            }
            String torGatewaysString = config.getProperty(TOR_GATEWAYS_PROP);
            if(torGatewaysString != null) {
                List<String> torGateways = Arrays.asList(torGatewaysString.split(","));
                for(String gateway : torGateways) {
                    this.torGateways.put(gateway, GatewayStatus.Active);
                }
            }
            String clearnetGatewaysString = config.getProperty(CLEARNET_GATEWAYS_PROP);
            if(clearnetGatewaysString != null) {
                List<String> clearGateways = Arrays.asList(clearnetGatewaysString.split(","));
                for(String gateway : clearGateways) {
                    this.clearnetGateways.put(gateway, GatewayStatus.Active);
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
