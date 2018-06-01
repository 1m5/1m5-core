package io.onemfive.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class OneMFiveVersion {

    private static final Logger LOG = Logger.getLogger(OneMFiveVersion.class.getName());

    /** deprecated */
    public final static String ID = "io.onemfive.core";
    public final static String VERSION = "0.5.0-SNAPSHOT";
    public final static long BUILD = 1;

    // TODO: Change to Maven Driven
    /** for example "-test" */
    public final static String EXTRA = "";
    public final static String FULL_VERSION = VERSION + "-" + BUILD + EXTRA;
    public static void main(String args[]) {
        print();
    }

    public static void print() {
        LOG.info("SC ID: " + ID);
        LOG.info("SC Version: " + FULL_VERSION);
    }
}
