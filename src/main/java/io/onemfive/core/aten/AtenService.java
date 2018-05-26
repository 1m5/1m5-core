package io.onemfive.core.aten;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.Aten;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class AtenService extends BaseService {

    private final Logger LOG = Logger.getLogger(AtenService.class.getName());

    public AtenService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Not implemented yet.");
        return true;
    }

}
