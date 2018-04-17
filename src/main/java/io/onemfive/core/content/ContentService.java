package io.onemfive.core.content;

import io.onemfive.core.bus.BaseService;
import io.onemfive.core.bus.MessageProducer;

import java.util.Properties;

/**
 * Created by Brian on 3/27/18.
 */
public class ContentService extends BaseService {

    public ContentService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("ContentService not implemented yet.");
        return true;
    }

}
