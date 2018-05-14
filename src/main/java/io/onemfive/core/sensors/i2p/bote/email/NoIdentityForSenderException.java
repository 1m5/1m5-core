package io.onemfive.core.sensors.i2p.bote.email;

import javax.mail.MessagingException;

public class NoIdentityForSenderException extends MessagingException {
    private String sender;

    public NoIdentityForSenderException(String sender) {
        super();
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }
}
