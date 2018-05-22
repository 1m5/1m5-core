package io.onemfive.core.ipfs;

import java.io.*;

public interface NamedStreamable
{
    InputStream getInputStream() throws IOException;

    String getName();

    boolean isDirectory();

}

