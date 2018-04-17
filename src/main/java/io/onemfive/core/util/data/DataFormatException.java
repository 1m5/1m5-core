package io.onemfive.core.util.data;

/*
 * free (adj.): unencumbered; not under the control of others
 * Written by jrandom in 2003 and released into the public domain
 * with no warranty of any kind, either expressed or implied.
 * It probably won't make your computer catch on fire, or eat
 * your children, but it might.  Use at your own risk.
 *
 */

/**
 * Thrown when the data was not available to read or write a DataStructure
 *
 * @author jrandom
 */
public class DataFormatException extends Exception {

    public DataFormatException(String msg, Throwable t) {
        super(msg, t);
    }

    public DataFormatException(String msg) {
        super(msg);
    }
}
