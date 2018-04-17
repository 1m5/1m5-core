/*
 * Created on Jul 16, 2004
 */
package io.onemfive.core.util.cpu;

/**
 * @author Iakin
 *
 */
public class UnknownCPUException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 5166144274582583742L;

    public UnknownCPUException() {
        super();
    }

    public UnknownCPUException(String message) {
        super(message);
    }
}
