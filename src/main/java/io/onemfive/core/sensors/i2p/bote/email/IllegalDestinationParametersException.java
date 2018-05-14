package io.onemfive.core.sensors.i2p.bote.email;

/**
 * Signifies that an Email Destination cannot be generated with the parameters provided.
 */
public class IllegalDestinationParametersException extends Exception {
    private static final long serialVersionUID = 8276866734402342858L;

    private char badChar;
    private String validChars;

    IllegalDestinationParametersException(char badChar, String validChars) {
        super();
        this.badChar = badChar;
        this.validChars = validChars;
    }

    public char getBadChar() {
        return badChar;
    }

    public String getValidChars() {
        return validChars;
    }
}
