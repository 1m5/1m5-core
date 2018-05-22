package io.onemfive.core.ipfs;

import java.io.*;
import java.net.URLEncoder;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class FileWrapper extends AbstractNamedStreamable {

    private final File source;
    private final String pathPrefix;

    public FileWrapper(String pathPrefix, File source) {
        this.source = source;
        this.pathPrefix = pathPrefix;
    }

    public FileWrapper(File source) {
        this("", source);
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(source);
    }

    public boolean isDirectory() {
        return source.isDirectory();
    }

    public File getFile() {
        return source;
    }

    public String getName() {
        try {
            return URLEncoder.encode(pathPrefix + source.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
