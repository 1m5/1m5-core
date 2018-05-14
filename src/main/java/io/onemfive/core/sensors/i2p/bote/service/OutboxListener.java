package io.onemfive.core.sensors.i2p.bote.service;

import io.onemfive.core.sensors.i2p.bote.email.Email;

public interface OutboxListener {

    void emailSent(Email email);
}
