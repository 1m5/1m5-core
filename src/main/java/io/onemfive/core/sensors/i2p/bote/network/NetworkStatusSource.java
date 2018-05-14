package io.onemfive.core.sensors.i2p.bote.network;

public interface NetworkStatusSource {

    void addNetworkStatusListener(NetworkStatusListener networkStatusListener);

    void removeNetworkStatusListener(NetworkStatusListener networkStatusListener);

    /**
     * Returns the status of initialization of the application.
     */
    NetworkStatus getNetworkStatus();

    /**
     * Returns an error that occurred during startup, or <code>null</code> for no error.
     */
    Exception getConnectError();

    /**
     * Returns <code>true</code> if the application has sucessfully
     * connected to I2P and the DHT.
     */
    boolean isConnected();
}
