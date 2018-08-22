package io.onemfive.core.infovault.storage;

import java.util.Properties;

public class StorageEngine {

    private Properties properties;

    public void init(Properties properties) {
        this.properties = properties;
    }

    public boolean teardown() {return true;}

}
