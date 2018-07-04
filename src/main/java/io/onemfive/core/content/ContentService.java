package io.onemfive.core.content;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatusListener;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides HTML5/CSS3/Node.js apps as a service using J2V8.
 * https://github.com/eclipsesource/j2v8
 *
 * @author objectorange
 */
public class ContentService extends BaseService {

    private static final Logger LOG = Logger.getLogger(ContentService.class.getName());

    public ContentService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Not implemented yet.");
        return true;
    }

}
