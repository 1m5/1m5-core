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

    private final Logger LOG = Logger.getLogger(SecureDropService.class.getName());

    public SecureDropService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(SecureDropService.class.getSimpleName()+": starting...");

        System.out.println(SecureDropService.class.getSimpleName()+": started.");
        return true;
    }
}
