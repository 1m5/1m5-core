package io.onemfive.core.content;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ContentService extends BaseService {

    private final Logger LOG = Logger.getLogger(ContentService.class.getName());

    public ContentService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(ContentService.class.getSimpleName()+": not implemented yet.");
        return true;
    }

}
