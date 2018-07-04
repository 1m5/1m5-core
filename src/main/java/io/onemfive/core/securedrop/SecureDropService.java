package io.onemfive.core.securedrop;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * https://securedrop.org/
 * http://secrdrop5wyphb5x.onion/
 *
 * @author objectorange
 */
public class SecureDropService extends BaseService {

    private static final Logger LOG = Logger.getLogger(SecureDropService.class.getName());

    public SecureDropService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
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
