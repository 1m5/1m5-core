package io.onemfive.core.consensus;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ConsensusService extends BaseService {

    public ConsensusService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(ConsensusService.class.getSimpleName()+": not implemented yet.");
        return true;
    }

}
