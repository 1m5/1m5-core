package io.onemfive.core.prana;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * User Utility Token for measuring and managing resource usage.
 *
 * @author ObjectOrange
 */
public class PranaService extends BaseService {

    private final Logger LOG = Logger.getLogger(PranaService.class.getName());

    public PranaService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(PranaService.class.getSimpleName()+": starting...");

        System.out.println(PranaService.class.getSimpleName()+": started.");
        return true;
    }
}
