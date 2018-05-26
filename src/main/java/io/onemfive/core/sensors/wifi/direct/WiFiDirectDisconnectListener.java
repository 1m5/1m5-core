package io.onemfive.core.sensors.wifi.direct;

import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class WiFiDirectDisconnectListener
//        implements WifiP2pManager.ActionListener
{

    private final Logger LOG = Logger.getLogger(WiFiDirectDisconnectListener.class.getName());

    private PeerDevice peerDevice;

    public WiFiDirectDisconnectListener(PeerDevice peerDevice) {
        this.peerDevice = peerDevice;
    }

//    @Override
//    public void onFailure(int reasonCode) {
//        Log.d(WiFiDirectDisconnectListener.class.getPackage().getName(), "Disconnect failed. Reason :" + reasonCode);
//
//    }
//
//    @Override
//    public void onSuccess() {
//        Log.d(WiFiDirectDisconnectListener.class.getPackage().getName(), "Connect succeeded.");
//    }
}
