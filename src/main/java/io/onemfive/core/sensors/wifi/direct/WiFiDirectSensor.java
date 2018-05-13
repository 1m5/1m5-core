package io.onemfive.core.sensors.wifi.direct;

//import android.app.IntentService;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.wifi.p2p.WifiP2pConfig;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.util.Log;

import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.Envelope;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class WiFiDirectSensor
        implements
        Sensor
//        WifiP2pManager.ChannelListener,
//        DeviceActionListener
{

//    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

//    private WifiP2pManager.Channel channel;
//    private BroadcastReceiver receiver = null;
//    private final IntentFilter intentFilter = new IntentFilter();

    private PeerDeviceList peerDeviceList;
    private PeerDevice peerDevice;

    public WiFiDirectSensor() {

    }

    @Override
    public boolean send(Envelope envelope) {
        return false;
    }

    //    @Override
//    protected void onHandleIntent(Intent intent) {
//        String message = "Event received by WiFi Direct Sensor.";
//        Log.v(WiFiDirectSensor.class.getPackage().getName(),message);
//        Notification notification = new Notification.Builder(this)
//                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
//                .setContentTitle("Sensor Event")
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setPriority(Notification.PRIORITY_MAX)
//                .setDefaults(Notification.DEFAULT_VIBRATE)
//                .build();
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(123458,notification);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//
//        peerDeviceList = new PeerDeviceList();
//        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = manager.initialize(this, getMainLooper(), null);
//        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
//        registerReceiver(receiver, intentFilter);
//    }

//    @Override
//    public void showDetails(WifiP2pDevice device) {
//
//    }
//
//    @Override
//    public void connect(WifiP2pConfig config) {
//        WifiP2pManager.ActionListener listener = new WiFiDirectConnectListener(this);
//        manager.connect(channel, config, listener);
//    }
//
//    @Override
//    public void disconnect() {
//        WifiP2pManager.ActionListener listener = new WiFiDirectDisconnectListener(peerDevice);
//        manager.removeGroup(channel, listener);
//    }
//
//    @Override
//    public void onChannelDisconnected() {
//        // we will try once more
//        if (manager != null && !retryChannel) {
//            Log.i(WiFiDirectSensor.class.getPackage().getName(),"Channel lost. Trying again");
//            resetData();
//            retryChannel = true;
//            manager.initialize(this, getMainLooper(), this);
//        } else {
//            Log.w(WiFiDirectSensor.class.getPackage().getName(),"Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.");
//        }
//    }

    /**
     * A cancel abort request by user. Disconnect i.e. removeGroup if
     * already connected. Else, request WifiP2pManager to abort the ongoing
     * request
     */
//    @Override
//    public void cancelDisconnect() {
//        if (manager != null) {
//            if (peerDeviceList.getDevice() == null
//                    || peerDeviceList.getDevice().status == WifiP2pDevice.CONNECTED) {
//                disconnect();
//            } else if (peerDeviceList.getDevice().status == WifiP2pDevice.AVAILABLE
//                    || peerDeviceList.getDevice().status == WifiP2pDevice.INVITED) {
//                WifiP2pManager.ActionListener listener = new WiFiDirectCancelConnectListener(this);
//                manager.cancelConnect(channel, listener);
//            }
//        }
//    }

    public PeerDeviceList getPeerDeviceList() {
        return peerDeviceList;
    }

    public PeerDevice getPeerDevice() {
        return peerDevice;
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        if (peerDeviceList != null) {
//            peerDeviceList.clearPeers();
        }
    }

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public boolean start(Properties properties) {
        return false;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean gracefulShutdown() {
        return false;
    }
}
