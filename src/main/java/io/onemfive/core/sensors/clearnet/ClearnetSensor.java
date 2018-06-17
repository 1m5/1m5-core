package io.onemfive.core.sensors.clearnet;

import io.onemfive.core.sensors.BaseSensor;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.Message;
import io.onemfive.data.util.DLC;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.Multipart;
import okhttp3.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public final class ClearnetSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(ClearnetSensor.class.getName());

    public static final String PROP_HTTP_CLIENT = "1m5.sensors.clearnet.http.client"; // true | false
    public static final String PROP_HTTP_CLIENT_TLS = "1m5.sensors.clearnet.http.client.tls"; // true | false
    public static final String PROP_HTTP_CLIENT_TLS_STRONG = "1m5.sensors.clearnet.http.client.tls.strong"; // true | false
    public static final String PROP_HTTP_SERVER = "1m5.sensors.clearnet.http.server"; // true | false
    public static final String PROP_HTTP_SERVER_IP = "1m5.sensors.clearnet.http.server.host"; // ipv4 | ipv6 address
    public static final String PROP_HTTP_SERVER_PORT = "1m5.sensors.clearnet.http.server.port"; // integer
    public static final String PROP_HTTP_SERVER_PATH = "1m5.sensors.clearnet.http.server.path"; // path

    private static final Set<String> trustedHosts = new HashSet<>();

    private static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private X509TrustManager x509TrustManager = new X509TrustManager() {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
    };

    // Create a trust manager that does not validate certificate chains
    private TrustManager[] trustAllCerts = new TrustManager[]{ x509TrustManager};

    private ConnectionSpec httpSpec;
    private OkHttpClient httpClient;

    private ConnectionSpec httpsCompatibleSpec;
    private OkHttpClient httpsCompatibleClient;

    private ConnectionSpec httpsStrongSpec;
    private OkHttpClient httpsStrongClient;

    private Server server;
    private HttpEnvelopeHandler httpHandler;

    public ClearnetSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    public boolean send(Envelope e) {
        URL url = e.getURL();
        if(url != null) {
            LOG.info("URL="+url.toString());
        }
        Map<String,Object> h = e.getHeaders();
        Map<String,String> hStr = new HashMap<>();
        if(h.containsKey(Envelope.HEADER_CONTENT_DISPOSITION)) {
            hStr.put(Envelope.HEADER_CONTENT_DISPOSITION,(String)h.get(Envelope.HEADER_CONTENT_DISPOSITION));
        }
        if(h.containsKey(Envelope.HEADER_CONTENT_TYPE)) {
            hStr.put(Envelope.HEADER_CONTENT_TYPE, (String)h.get(Envelope.HEADER_CONTENT_TYPE));
        }
        if(h.containsKey(Envelope.HEADER_CONTENT_TRANSFER_ENCODING)) {
            hStr.put(Envelope.HEADER_CONTENT_TRANSFER_ENCODING, (String)h.get(Envelope.HEADER_CONTENT_TRANSFER_ENCODING));
        }
        if(h.containsKey(Envelope.HEADER_USER_AGENT)) {
            hStr.put(Envelope.HEADER_USER_AGENT, (String)h.get(Envelope.HEADER_USER_AGENT));
        }

        ByteBuffer bodyBytes = null;
        CacheControl cacheControl = null;
        if(e.getMultipart() != null) {
            // handle file upload
            Multipart m = e.getMultipart();
            hStr.put(Envelope.HEADER_CONTENT_TYPE, "multipart/form-data; boundary=" + m.getBoundary());
            try {
                bodyBytes = ByteBuffer.wrap(m.finish().getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
                // TODO: Provide error message
                LOG.warning("IOException caught while building HTTP body with multipart: "+e1.getLocalizedMessage());
                return false;
            }
            cacheControl = new CacheControl.Builder().noCache().build();
        }
        Headers headers = Headers.of(hStr);

        Message m = e.getMessage();
        if(m instanceof DocumentMessage) {
            Object contentObj = DLC.getContent(e);
            if(contentObj instanceof String) {
                if(bodyBytes == null) {
                    bodyBytes = ByteBuffer.wrap(((String)contentObj).getBytes());
                } else {
                    bodyBytes.put(((String)contentObj).getBytes());
                }
            } else if(contentObj instanceof byte[]) {
                if(bodyBytes == null) {
                    bodyBytes = ByteBuffer.wrap((byte[])contentObj);
                } else {
                    bodyBytes.put((byte[])contentObj);
                }
            }
        } else {
            LOG.warning("Only DocumentMessages supported at this time.");
            DLC.addErrorMessage("Only DocumentMessages supported at this time.",e);
            return false;
        }

        RequestBody requestBody = null;
        if(bodyBytes != null) {
            requestBody = RequestBody.create(MediaType.parse((String) h.get(Envelope.HEADER_CONTENT_TYPE)), bodyBytes.array());
        }

        Request.Builder b = new Request.Builder().url(url);
        if(cacheControl != null)
            b = b.cacheControl(cacheControl);
        b = b.headers(headers);
        switch(e.getAction()) {
            case ADD: {b = b.post(requestBody);break;}
            case UPDATE: {b = b.put(requestBody);break;}
            case REMOVE: {b = (requestBody == null ? b.delete() : b.delete(requestBody));break;}
            case VIEW: {b = b.get();break;}
            default: {
                LOG.warning("Envelope.action must be set to ADD, UPDATE, REMOVE, or VIEW");
                return false;
            }
        }
        Request req = b.build();
        if(req == null) {
            LOG.warning("okhttp3 builder didn't build request.");
            return false;
        }
        LOG.info("Sending http request, host="+url.getHost());
        Response response = null;
        if(url.toString().startsWith("https:")) {
//            if(trustedHosts.contains(url.getHost())) {
                try {
//                    LOG.info("Trusted host, using compatible connection...");
                    response = httpsCompatibleClient.newCall(req).execute();
                    if(!response.isSuccessful()) {
                        LOG.warning(response.toString());
                        m.addErrorMessage(response.code()+"");
                        return false;
                    }
                } catch (IOException e1) {
                    m.addErrorMessage(e1.getLocalizedMessage());
                    return false;
                }
//            } else {
//                try {
//                    System.out.println(ClearnetSensor.class.getSimpleName() + ": using strong connection...");
//                    response = httpsStrongClient.newCall(req).execute();
//                    if (!response.isSuccessful()) {
//                        m.addErrorMessage(response.code()+"");
//                        return false;
//                    }
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                    m.addErrorMessage(ex.getLocalizedMessage());
//                    return false;
//                }
//            }
        } else {
            if(httpClient == null) {
                LOG.severe("httpClient was not set up.");
                return false;
            }
            try {
                response = httpClient.newCall(req).execute();
                if(!response.isSuccessful()) {
                    m.addErrorMessage(response.code()+"");
                    return false;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                m.addErrorMessage(e2.getLocalizedMessage());
                return false;
            }
        }

        LOG.info("Received http response.");
        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            LOG.info(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }
        ResponseBody responseBody = response.body();
        if(responseBody != null) {
            try {
                DLC.addContent(responseBody.bytes(),e);
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                responseBody.close();
            }
            LOG.info(new String((byte[])DLC.getContent(e)));
        } else {
            LOG.info("Body was null.");
            DLC.addContent(null,e);
        }

        return true;
    }

    void sendToBus(Envelope envelope) {
        sensorsService.sendToBus(envelope);
    }

    @Override
    public boolean reply(Envelope e) {
        httpHandler.reply(e);
        return true;
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");

        if("true".equals(properties.getProperty(PROP_HTTP_CLIENT))) {
            httpSpec = new ConnectionSpec
                    .Builder(ConnectionSpec.CLEARTEXT)
                    .build();
            httpClient = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(httpSpec))
                    .retryOnConnectionFailure(true)
                    .followRedirects(true)
                    .build();

            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                httpsCompatibleSpec = new ConnectionSpec
                        .Builder(ConnectionSpec.COMPATIBLE_TLS)
//                    .supportsTlsExtensions(true)
//                    .allEnabledTlsVersions()
//                    .allEnabledCipherSuites()
                        .build();

                httpsCompatibleClient = new OkHttpClient.Builder()
//                    .connectionSpecs(Collections.singletonList(httpsCompatibleSpec))
//                    .retryOnConnectionFailure(false)
//                    .followSslRedirects(false)
                        .sslSocketFactory(sc.getSocketFactory(), x509TrustManager)
                        .hostnameVerifier(hostnameVerifier)
                        .build();

                httpsStrongSpec = new ConnectionSpec
                        .Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                        .cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                        .build();

                httpsStrongClient = new OkHttpClient.Builder()
                        .connectionSpecs(Collections.singletonList(httpsStrongSpec))
                        .retryOnConnectionFailure(true)
                        .followSslRedirects(true)
                        .sslSocketFactory(sc.getSocketFactory(), x509TrustManager)
                        .hostnameVerifier(hostnameVerifier)
                        .build();

            } catch (Exception e) {
                e.printStackTrace();
                LOG.warning(e.getLocalizedMessage());
            }
        }

        if("true".equals(properties.getProperty(PROP_HTTP_SERVER))) {

            ContextHandler context = new ContextHandler();
            context.setContextPath( "/" );
            context.setHandler( new HttpEnvelopeHandler(this) );

            String host = "127.0.0.1";
//            String hostProp = properties.getProperty(PROP_HTTP_SERVER_IP);
//            if(hostProp != null && !"".equals(hostProp)) {
//                host = hostProp;
//            }
            LOG.info("HTTP Server Host: "+host);

            int port = 8080;
//            String portStr = properties.getProperty(PROP_HTTP_SERVER_PORT);
//            if(portStr != null) {
//                port = Integer.parseInt(portStr);
//            }
            LOG.info("HTTP Server Port: "+port);

            InetSocketAddress addr = new InetSocketAddress(host,port);
            server = new Server(addr);
            server.setHandler(context);

            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            server.dumpStdErr();
            try {
                // The use of server.join() the will make the current thread join and
                // wait until the server is done executing.
                // See
                // http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
                server.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LOG.info("Started.");
        return true;
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
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

    public static void main(String[] args) {
//        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
//        System.setProperty("https.protocols", "TLSv1");
//        System.setProperty("javax.net.debug", "all");
        Envelope e = Envelope.documentFactory();
        e.setAction(Envelope.Action.VIEW);
        e.setHeader(Envelope.HEADER_CONTENT_TYPE,"text/plain; charset=utf-8");
        URL url;
        boolean ipfs = true;
        boolean snapshot = true;
        String hash = "QmTDMoVqvyBkNMRhzvukTDznntByUNDwyNdSfV8dZ3VKRC/readme.md";
        try {
            if (snapshot) {
//                url = new URL("https", "ipfs.io", 443, "/ipfs/" + hash);
                url = new URL("https://ipfs.io/ipfs/" + hash);
            } else {
                url = new URL("https", "ipfs.io", 443, "/ipfn/" + hash);
            }
            e.setURL(url);
            ClearnetSensor sensor = new ClearnetSensor(null);
            sensor.start(null);
            sensor.send(e);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

//        e.setHeader(Envelope.HEADER_CONTENT_TYPE,"application/json; charset=utf-8");
//        try {
//            e.setURL(new URL("https://ipfs.github.io/public-gateway-checker/gateways.json"));
//        } catch (MalformedURLException e1) {
//            e1.printStackTrace();
//        }
//        new ClearnetSensor().send(e);

//        e.setHeader(Envelope.HEADER_CONTENT_TYPE,"plain/text; charset=utf-8");
//        try {
//            e.setURL(new URL("https://1m5.io"));
//        } catch (MalformedURLException e1) {
//            e1.printStackTrace();
//        }
//        new ClearnetSensor().send(e);
    }
}
