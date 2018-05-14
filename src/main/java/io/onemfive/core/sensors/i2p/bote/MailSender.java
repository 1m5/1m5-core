package io.onemfive.core.sensors.i2p.bote;

import io.onemfive.core.sensors.i2p.bote.email.Email;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.mail.MessagingException;

import net.i2p.data.DataFormatException;

public interface MailSender {

    void sendEmail(Email email) throws MessagingException, PasswordException, IOException, GeneralSecurityException;
}
