package io.onemfive.core.aten;

import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.MessageProducer;

import java.util.Properties;

/**
 * Created by Brian on 3/27/18.
 */
public class AtenService extends BaseService {

    public AtenService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("AtenService not implemented yet.");
        return true;
    }

}
