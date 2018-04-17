package io.onemfive.core.sensors.wifi.direct;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 *
 * @author objectorange
 */
public class WiFiDirectFileTransferService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_ADDRESS = "go_host";
    public static final String EXTRAS_PORT = "go_port";

    public WiFiDirectFileTransferService() {

    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//        Context context = getApplicationContext();
//        if (intent.getAction().equals(ACTION_SEND_FILE)) {
//            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
//            String host = intent.getExtras().getString(EXTRAS_ADDRESS);
//            Socket socket = new Socket();
//            int port = intent.getExtras().getInt(EXTRAS_PORT);
//
//            try {
//                Log.d(WiFiDirectFileTransferService.class.getPackage().getName(), "Opening client socket - ");
//                socket.bind(null);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
//
//                Log.d(WiFiDirectFileTransferService.class.getPackage().getName(), "Client socket - " + socket.isConnected());
//                OutputStream stream = socket.getOutputStream();
//                ContentResolver cr = context.getContentResolver();
//                InputStream is = null;
//                try {
//                    is = cr.openInputStream(Uri.parse(fileUri));
//                } catch (FileNotFoundException e) {
//                    Log.d(WiFiDirectFileTransferService.class.getPackage().getName(), e.toString());
//                }
//                PeerDevice.copyFile(is, stream);
//                Log.d(WiFiDirectFileTransferService.class.getPackage().getName(), "Client: Data written");
//            } catch (IOException e) {
//                Log.e(WiFiDirectFileTransferService.class.getPackage().getName(), e.getMessage());
//            } finally {
//                if (socket != null) {
//                    if (socket.isConnected()) {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            // Give up
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        }
//    }
}
