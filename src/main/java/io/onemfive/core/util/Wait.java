package io.onemfive.core.util;

import java.util.concurrent.TimeUnit;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Wait {

    public static void aDay(long waitTimeDays) {
        try {
            TimeUnit.DAYS.sleep(waitTimeDays);
        } catch (InterruptedException e) {}
    }

    public static void aHour(long waitTimeHours) {
        try {
            TimeUnit.HOURS.sleep(waitTimeHours);
        } catch (InterruptedException e) {}
    }

    public static void aMin(long waitTimeMinutes) {
        try {
            TimeUnit.MINUTES.sleep(waitTimeMinutes);
        } catch (InterruptedException e) {}
    }

    public static void aSec(long waitTimeSeconds) {
        try {
            TimeUnit.SECONDS.sleep(waitTimeSeconds);
        } catch (InterruptedException e) {}
    }

    public static void aMs(long waitTimeMilliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(waitTimeMilliseconds);
        } catch (InterruptedException e) {}
    }

    public static void aMic(long waitTimeMicroseconds) {
        try {
            TimeUnit.MICROSECONDS.sleep(waitTimeMicroseconds);
        } catch (InterruptedException e) {}
    }
}
