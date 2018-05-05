package io.onemfive.core.aten;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.Aten;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class AtenService extends BaseService {

    public AtenService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(AtenService.class.getSimpleName()+": not implemented yet.");
        return true;
    }

}
