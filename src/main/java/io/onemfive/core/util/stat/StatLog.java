package io.onemfive.core.util.stat;

/**
 * Component to be notified when a particular event occurs
 */
public interface StatLog {
    void addData(String scope, String stat, long value, long duration);
}
