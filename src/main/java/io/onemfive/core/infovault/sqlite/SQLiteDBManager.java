package io.onemfive.core.infovault.sqlite;

import io.onemfive.core.LifeCycle;

import java.util.Properties;

public class SQLiteDBManager implements LifeCycle {

    public SQLiteDBManager() {

    }


    @Override
    public boolean start(Properties properties) {
        return false;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean gracefulShutdown() {
        return false;
    }
}
