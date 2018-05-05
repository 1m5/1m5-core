package io.onemfive.core.content;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ContentService extends BaseService {

    public ContentService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(ContentService.class.getSimpleName()+": not implemented yet.");
        return true;
    }

}
