package io.onemfive.core.sensors.wifi.direct;

//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pInfo;
//import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
//import android.os.AsyncTask;
//import android.os.Environment;
//import android.util.Log;
//import android.view.View;
/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 *
 * @author objectorange
 */
public class PeerDevice
//        implements ConnectionInfoListener
{

    public static final String IP_PEER = "192.168.49.1";
    public static int PORT = 8988;
    private static boolean consumerRunning = false;

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
//    private View mContentView = null;
//    private WifiP2pDevice device;
//    private WifiP2pInfo info;

//    @Override
//    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
//        this.info = info;
//
//        // The owner IP is now known.
//        boolean isGroupOwner = info.isGroupOwner;
//
//        // InetAddress from WifiP2pInfo struct.
//        String hostAddress = info.groupOwnerAddress.getHostAddress();
//
//        if (!consumerRunning){
//            new ConsumerPeerAsyncTask().execute();
//            consumerRunning = true;
//        }
//
//    }

    /**
     * A simple socket that accepts connection and writes some data on the stream.
     */
//    public static class ConsumerPeerAsyncTask extends AsyncTask<Void, Void, String> {
//
//
//        public ConsumerPeerAsyncTask() {
//
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            try {
//                ServerSocket serverSocket = new ServerSocket(PORT);
//                Log.d(PeerDevice.class.getPackage().getName(), "Consuming socket opened");
//                Socket client = serverSocket.accept();
//                Log.d(PeerDevice.class.getPackage().getName(), "Consuming connection done");
//                final File f = new File(Environment.getExternalStorageDirectory()
//                        + "/temp/wifip2pshared-"
//                        + System.currentTimeMillis()
//                        + ".jpg");
//
//                File dirs = new File(f.getParent());
//                if (!dirs.exists())
//                    dirs.mkdirs();
//                f.createNewFile();
//
//                Log.d(PeerDevice.class.getPackage().getName(), "Consuming file " + f.toString());
//                InputStream inputstream = client.getInputStream();
//                copyFile(inputstream, new FileOutputStream(f));
//                serverSocket.close();
//                consumerRunning = false;
//                return f.getAbsolutePath();
//            } catch (IOException e) {
//                Log.e(PeerDevice.class.getPackage().getName(), e.getMessage());
//                return null;
//            }
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
//         */
//        @Override
//        protected void onPostExecute(String result) {
//            if (result != null) {
//                Log.i(PeerDevice.class.getPackage().getName(),"File consumed - " + result);
//            }
//
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPreExecute()
//         */
//        @Override
//        protected void onPreExecute() {
//            Log.i(PeerDevice.class.getPackage().getName(),"Opening a consuming socket...");
//        }
//
//    }

//    public static boolean copyFile(InputStream inputStream, OutputStream out) {
//        byte buf[] = new byte[1024];
//        int len;
//        try {
//            while ((len = inputStream.read(buf)) != -1) {
//                out.write(buf, 0, len);
//
//            }
//            out.close();
//            inputStream.close();
//        } catch (IOException e) {
//            Log.d(PeerDevice.class.getPackage().getName(), e.toString());
//            return false;
//        }
//        return true;
//    }

}
