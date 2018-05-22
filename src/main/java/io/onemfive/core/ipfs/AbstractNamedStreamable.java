package io.onemfive.core.ipfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class AbstractNamedStreamable implements NamedStreamable {

    public byte[] getContents() throws IOException {
        InputStream in = getInputStream();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        int r;
        while ((r=in.read(tmp))>= 0)
            bout.write(tmp, 0, r);
        return bout.toByteArray();
    }

}
