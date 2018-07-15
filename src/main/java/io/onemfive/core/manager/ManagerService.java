package io.onemfive.core.manager;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages the bus and its services including auto-install of new services,
 * auto-updates, and auto-uninstalls.
 *
 * @author objectorange
 */
public class ManagerService extends BaseService {

    private static final Logger LOG = Logger.getLogger(ManagerService.class.getName());

    public ManagerService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down....");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        LOG.info("Gracefully shutting down....");
        updateStatus(ServiceStatus.GRACEFULLY_SHUTTING_DOWN);

        updateStatus(ServiceStatus.GRACEFULLY_SHUTDOWN);
        LOG.info("Gracefully Shutdown.");
        return true;
    }
}
