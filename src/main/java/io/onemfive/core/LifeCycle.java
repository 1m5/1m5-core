package io.onemfive.core;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface LifeCycle {
    boolean start(Properties properties);
    boolean pause();
    boolean unpause();// note: resume method name conflicts with Thread
    boolean restart();
    boolean shutdown();
    boolean gracefulShutdown();
}
