package io.onemfive.core.notification;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.data.Envelope;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides notifications of publishing events for subscribers.
 *
 * @author objectorange
 */
public class NotificationService extends BaseService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    public static final String OPERATION_SUBSCRIBE = "SUBSCRIBE";
    public static final String OPERATION_PUBLISH = "PUBLISH";

    

    public NotificationService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleEvent(Envelope envelope) {

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
