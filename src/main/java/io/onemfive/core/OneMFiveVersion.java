package io.onemfive.core;

import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class OneMFiveVersion {

    private final Logger LOG = Logger.getLogger(OneMFiveVersion.class.getName());

    /** deprecated */
    public final static String ID = "io.synapticcelerity.core";
    public final static String VERSION = "0.3.2";
    public final static long BUILD = 1;

    // TODO: Change to Maven Driven
    /** for example "-test" */
    public final static String EXTRA = "";
    public final static String FULL_VERSION = VERSION + "-" + BUILD + EXTRA;
    public static void main(String args[]) {
        print();
    }

    public static void print() {
        System.out.println("SC ID: " + ID);
        System.out.println("SC Version: " + FULL_VERSION);
    }
}
