package io.onemfive.core.prana;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * User Utility Token for measuring and managing resource usage.
 *
 * @author ObjectOrange
 */
public class PranaService extends BaseService {

    public PranaService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("PranaService starting...");

        System.out.println("PranaService started.");
        return true;
    }
}
