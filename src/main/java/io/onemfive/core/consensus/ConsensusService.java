package io.onemfive.core.consensus;

import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.MessageProducer;

import java.util.Properties;

/**
 * Created by Brian on 3/27/18.
 */
public class ConsensusService extends BaseService {

    public ConsensusService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("ConsensusService not implemented yet.");
        return true;
    }

}
