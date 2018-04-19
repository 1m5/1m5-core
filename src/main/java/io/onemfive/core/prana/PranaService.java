package io.onemfive.core.prana;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * Created by Brian on 3/27/18.
 */
public class PranaService extends BaseService {

    public PranaService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("PranaService not implemented yet.");
        return true;
    }
}
