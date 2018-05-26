package io.onemfive.core.consensus;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ConsensusService extends BaseService {

    private static final Logger LOG = Logger.getLogger(ConsensusService.class.getName());

    public ConsensusService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Not implemented yet.");
        return true;
    }

}
