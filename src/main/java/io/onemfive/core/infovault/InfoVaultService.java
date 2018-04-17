package io.onemfive.core.infovault;

import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.MessageProducer;

import java.util.Properties;

/**
 * Created by Brian on 3/27/18.
 */
public class InfoVaultService extends BaseService {

    public InfoVaultService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("InfoVaultService not implemented yet.");
        return true;
    }
}
