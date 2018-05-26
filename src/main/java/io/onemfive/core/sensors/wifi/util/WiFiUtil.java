package io.onemfive.core.sensors.wifi.util;

import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class WiFiUtil {

    private final Logger LOG = Logger.getLogger(WiFiUtil.class.getName());

    /**
     * distance = 10 ^ ((27.55 - (20 * log10(frequency)) + signalLevel)/20)
     * Example: frequency = 2412MHz, signalLevel = -57dbm, result = 7.000397427391188m
     * This formula is transformed form of Free Space Path Loss(FSPL) formula.
     * Here the distance is measured in meters and the frequency - in megahertz.
     * For other measures you have to use different constant (27.55).
     * Background: https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
     * @param signalLevelInDb
     * @param freqInMHz
     * @return
     */
    public static double calculateDistanceInMeters(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
