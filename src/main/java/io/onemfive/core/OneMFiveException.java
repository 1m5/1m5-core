package io.onemfive.core;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class OneMFiveException extends Exception {

    public OneMFiveException() {
    }

    public OneMFiveException(String message) {
        super(message);
    }

    public OneMFiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public OneMFiveException(Throwable cause) {
        super(cause);
    }
}
