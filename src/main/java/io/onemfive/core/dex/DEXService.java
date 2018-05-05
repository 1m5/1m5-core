package io.onemfive.core.dex;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * Decentralized EXchange Service
 *
 * @author objectorange
 */
public class DEXService extends BaseService {

    public DEXService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(DEXService.class.getSimpleName()+": not implemented yet.");
        return true;
    }
}
