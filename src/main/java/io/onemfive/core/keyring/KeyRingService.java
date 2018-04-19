package io.onemfive.core.keyring;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * Manages and secures keys.
 *
 * @author ObjectOrange
 */
public class KeyRingService extends BaseService {

    public KeyRingService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("KeyRingService starting...");

        System.out.println("KeyRingService started.");
        return true;
    }

}
