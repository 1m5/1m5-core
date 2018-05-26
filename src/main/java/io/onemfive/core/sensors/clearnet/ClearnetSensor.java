package io.onemfive.core.sensors.clearnet;

import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.Message;
import io.onemfive.data.util.DLC;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.Multipart;
import okhttp3.*;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public final class ClearnetSensor implements Sensor {

    private static final Logger LOG = Logger.getLogger(ClearnetSensor.class.getName());

    private static final Set<String> trustedHosts = new HashSet<>();

    private static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            if(trustedHosts.contains(hostname)) {
                LOG.info("Trusted Host :" + hostname);
                return true;
            } else {
                LOG.warning("Untrusted Host :" + hostname);
                return false;
            }
        }
    };

    static {
        trustedHosts.add("ipfs.io");
        trustedHosts.add("ipfs.github.io");
        trustedHosts.add("1m5.io");
    }

    private final ConnectionSpec httpSpec = new ConnectionSpec
            .Builder(ConnectionSpec.CLEARTEXT)
            .build();

    private final ConnectionSpec httpsCompatibleSpec = new ConnectionSpec
            .Builder(ConnectionSpec.COMPATIBLE_TLS)
            .supportsTlsExtensions(true)
            .allEnabledTlsVersions()
            .allEnabledCipherSuites()
            .build();

    private final ConnectionSpec httpsStrongSpec = new ConnectionSpec
            .Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
            .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
            .build();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(httpSpec))
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .build();

    private final OkHttpClient httpsCompatibleClient = new OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(httpsCompatibleSpec))
            .retryOnConnectionFailure(true)
            .followSslRedirects(true)
            .hostnameVerifier(hostnameVerifier)
            .build();

    private final OkHttpClient httpsStrongClient = new OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(httpsStrongSpec))
            .retryOnConnectionFailure(true)
            .followSslRedirects(true)
            .build();

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
        LOG.info("Sending http request, host="+url.getHost());
        Response response = null;
        if(url.toString().startsWith("https:")) {
            if(trustedHosts.contains(url.getHost())) {
                try {
                    LOG.info("Trusted host, using compatible connection...");
                    response = httpsCompatibleClient.newCall(req).execute();
                    if(!response.isSuccessful()) {
                        LOG.warning("Unexpected code " + response);
                        return false;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                    LOG.warning("Compatible connection attempt failed: "+e1.getLocalizedMessage());
                    return false;
                }
            } else {
                try {
                    System.out.println(ClearnetSensor.class.getSimpleName() + ": using strong connection...");
                    response = httpsStrongClient.newCall(req).execute();
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    LOG.warning("Strong connection attempt failed: "+ex.getLocalizedMessage());
                    return false;
                }
            }
        } else {
            try {
                response = httpClient.newCall(req).execute();
                if(!response.isSuccessful()) {
                    LOG.warning("Unexpected code " + response);
                    return false;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                LOG.warning("Light connection attempt failed. Giving up. "+e2.getLocalizedMessage());
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
                url = new URL("https", "ipfs.io", 443, "/ipfs/" + hash);
            } else {
                url = new URL("https", "ipfs.io", 443, "/ipfn/" + hash);
            }
            e.setURL(url);
            new ClearnetSensor().send(e);
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
