package io.onemfive.core.sensors.wifi.direct;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class WiFiDirectDisconnectListener
//        implements WifiP2pManager.ActionListener
{

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
