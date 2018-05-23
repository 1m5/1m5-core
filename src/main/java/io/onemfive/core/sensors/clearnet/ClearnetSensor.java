package io.onemfive.core.sensors.clearnet;

import io.onemfive.core.sensors.Sensor;
import io.onemfive.data.Message;
import io.onemfive.data.util.DLC;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.Multipart;
import okhttp3.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

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
                // TODO: Replace with error message
                return false;
            }
            cacheControl = new CacheControl.Builder().noCache().build();
        }
        Headers headers = Headers.of(hStr);

        Message m = e.getMessage();
        if(m instanceof DocumentMessage) {
            Object contentObj = ((DocumentMessage)m).data.get(0).get(DLC.CONTENT);
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
            System.out.println(ClearnetSensor.class.getSimpleName()+": Only DocumentMessages supported at this time.");
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
                System.out.println(ClearnetSensor.class.getSimpleName()+": Envelope.action must be set to ADD, UPDATE, REMOVE, or VIEW");
                return false;
            }
        }
        Request req = b.build();

        try (Response response = c.newCall(req).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }
            ResponseBody responseBody = response.body();
            if(responseBody != null) {
                System.out.println(responseBody.string());
                ((DocumentMessage)e.getMessage()).data.get(0).put(DLC.CONTENT,responseBody.bytes());
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
