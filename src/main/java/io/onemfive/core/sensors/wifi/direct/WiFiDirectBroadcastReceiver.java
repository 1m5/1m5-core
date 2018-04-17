package io.onemfive.core.sensors.wifi.direct;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class WiFiDirectBroadcastReceiver
//        extends BroadcastReceiver
{

//    private WifiP2pManager manager;
//    private Channel channel;
//    private WiFiDirectSensor sensor;
//
//    /**
//     * @param manager WifiP2pManager system service
//     * @param channel Wifi p2p channel
//     * @param sensor sensor associated with the receiver
//     */
//    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WiFiDirectSensor sensor) {
//        super();
//        this.manager = manager;
//        this.channel = channel;
//        this.sensor = sensor;
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
//     * android.content.Intent)
//     */
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        switch(intent.getAction()) {
//            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION : {
//                // UI update to indicate wifi p2p status.
//                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                    // Wifi Direct mode is enabled
//                    sensor.setIsWifiP2pEnabled(true);
//                } else {
//                    sensor.setIsWifiP2pEnabled(false);
//                    sensor.resetData();
//                }
//                Log.d(WiFiDirectBroadcastReceiver.class.getPackage().getName(), "P2P state changed - " + state);
//                break;
//            }
//            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION : {
//                // request available peers from the wifi p2p manager. This is an
//                // asynchronous call and the calling activity is notified with a
//                // callback on PeerListListener.onPeersAvailable()
//                if (manager != null) {
//                    manager.requestPeers(channel, sensor.getPeerDeviceList());
//                }
//                Log.d(WiFiDirectBroadcastReceiver.class.getPackage().getName(), "P2P peers changed");
//                break;
//            }
//            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION : {
//                if (manager == null) return;
//
//                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//
//                if (networkInfo.isConnected()) {
//                    // we are connected with the other device, request connection
//                    // info to find group owner IP
//                    PeerDevice peerDevice = sensor.getPeerDevice();
//                    manager.requestConnectionInfo(channel, peerDevice);
//                } else {
//                    // It's a disconnect
//                    sensor.resetData();
//                }
//                break;
//            }
//            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION : {
//                PeerDeviceList peerDeviceList = sensor.getPeerDeviceList();
//                if(peerDeviceList != null) {
//                    peerDeviceList.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
//                } else {
//                    Log.d(WiFiDirectBroadcastReceiver.class.getPackage().getName(), "Peer Device List not found.");
//                }
//
//            }
//        }
//    }
}
