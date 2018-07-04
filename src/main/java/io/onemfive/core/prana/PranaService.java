package io.onemfive.core.prana;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * User Utility Token for measuring and managing resource usage.
 *
 * @author ObjectOrange
 */
public class PranaService extends BaseService {

    private static final Logger LOG = Logger.getLogger(PranaService.class.getName());

    public PranaService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }
}
