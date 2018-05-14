package io.onemfive.core.sensors.i2p.bote.service;

public interface ApiService {
    int IMAP = 0;
    int SMTP = 1;

    void start(int type);
    void stop(int type);
    void stopAll();
    void printRunningThreads();
}
