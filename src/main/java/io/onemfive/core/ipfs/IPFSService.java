package io.onemfive.core.ipfs;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.Envelope;

import java.util.*;

/**
 * Abstracts IPFS as an internal Service.
 *
 * @author objectorange
 */
public class IPFSService extends BaseService {

    public static final String OPERATION_LOAD = "LOAD";
    public static final String OPERATION_SEARCH = "SEARCH";

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

    }

    private void load(Envelope envelope) {

    }

    private void search(Envelope envelope) {
        
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
