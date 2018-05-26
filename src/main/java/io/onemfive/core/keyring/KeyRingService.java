package io.onemfive.core.keyring;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages and secures keys.
 *
 * @author ObjectOrange
 */
public class KeyRingService extends BaseService {

    private final Logger LOG = Logger.getLogger(KeyRingService.class.getName());

    public KeyRingService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(KeyRingService.class.getSimpleName()+": starting...");

        System.out.println(KeyRingService.class.getSimpleName()+": started.");
        return true;
    }

}
