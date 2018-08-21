package io.onemfive.core.infovault;

import io.onemfive.core.*;
import io.onemfive.data.*;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.logging.Logger;

/**
 * Asynchronous access to persistence.
 * Access to the instance of InfoVaultDB is provided in each Service too (by BaseService) for synchronous access.
 * Developer's choice to which to use on a per-case basis by Services extending BaseService.
 * Clients always use this service as they do not have direct access to InfoVaultDB.
 * Consider using this service for heavier higher-latency work by Services extending BaseService vs using their
 * synchronous access instance in BaseService.
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    private static final Logger LOG = Logger.getLogger(InfoVaultService.class.getName());

    public static final String OPERATION_EXECUTE = "EXECUTE";

    public InfoVaultService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        switch(r.getOperation()) {
            case OPERATION_EXECUTE: {execute(e);break;}
            default: deadLetter(e);
        }
    }

    private void execute(Envelope e) {
        DAO dao = (DAO)DLC.getData(DAO.class, e);
        dao.execute();
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

}
