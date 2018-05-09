package io.onemfive.core.securedrop;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * https://securedrop.org/
 * http://secrdrop5wyphb5x.onion/
 *
 * @author objectorange
 */
public class SecureDropService extends BaseService {

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
