package io.onemfive.core.util;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Wait {

    public static void waitABit(long waitTimeMs) {
        try {
            Thread.sleep(waitTimeMs);
        } catch (InterruptedException e) {}
    }
}
