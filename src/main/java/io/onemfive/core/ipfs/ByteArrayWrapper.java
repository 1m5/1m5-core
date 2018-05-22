package io.onemfive.core.ipfs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ByteArrayWrapper extends AbstractNamedStreamable {

    private final String name;
    private final byte[] data;

    public ByteArrayWrapper(byte[] data) {
        this(null, data);
    }

    public ByteArrayWrapper(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public boolean isDirectory() {
        return false;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    public String getName() {
        return name;
    }
}
