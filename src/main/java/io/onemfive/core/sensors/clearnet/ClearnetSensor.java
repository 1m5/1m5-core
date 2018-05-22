package io.onemfive.core.sensors.clearnet;

import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.util.DLC;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import okhttp3.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ClearnetSensor implements Sensor {

    private final OkHttpClient c = new OkHttpClient();

    @Override
    public boolean send(Envelope e) {
        URL url = e.getURL();
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
        Headers headers = Headers.of(hStr);
        Request req = new Request.Builder()
                .url(url)
                .headers(headers)
                .build();
        try (Response response = c.newCall(req).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }
            ResponseBody body = response.body();
            if(body != null) {
                System.out.println(body.string());
                ((DocumentMessage)e.getMessage()).data.get(0).put(DLC.CONTENT,body);
            } else {
                ((DocumentMessage)e.getMessage()).data.get(0).put(DLC.CONTENT,DLC.NONE);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
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
        Envelope e = Envelope.documentFactory();
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
    }
}
