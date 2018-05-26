package io.onemfive.core.securedrop;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

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

    public SecureDropService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");

        LOG.info("Started.");
        return true;
    }
}
