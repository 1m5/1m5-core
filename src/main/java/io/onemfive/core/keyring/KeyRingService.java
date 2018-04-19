package io.onemfive.core.keyring;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * Created by Brian on 3/27/18.
 */
public class KeyRingService extends BaseService {

    public KeyRingService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("KeyRingService not implemented yet.");
        return true;
    }

}
