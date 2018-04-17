package io.onemfive.core.util;

import java.io.File;
import java.io.IOException;

/**
 * Same as SecureDirectory but sets the file mode after createNewFile()
 * and createTempFile() also. So just use this instead.
 * Probably should have just made this class in the beginning and not had two.
 *
 * @author I2P - zzz
 */
public class SecureFile extends File {

    protected static final boolean isNotWindows = !SystemVersion.isWindows();

    public SecureFile(String pathname) {
        super(pathname);
    }

    public SecureFile(String parent, String child) {
        super(parent, child);
    }

    public SecureFile(File parent, String child) {
        super(parent, child);
    }

    /**
     *  Sets file to mode 600 if the file is created
     */
    @Override
    public boolean createNewFile() throws IOException {
        boolean rv = super.createNewFile();
        if (rv)
            setPerms();
        return rv;
    }

    /**
     *  Sets directory to mode 700 if the directory is created
     */
    @Override
    public boolean mkdir() {
        boolean rv = super.mkdir();
        if (rv)
            setPerms();
        return rv;
    }

    /**
     *  Sets directory to mode 700 if the directory is created
     *  Does NOT change the mode of other created directories
     */
    @Override
    public boolean mkdirs() {
        boolean rv = super.mkdirs();
        if (rv)
            setPerms();
        return rv;
    }

    /**
     *  Sets file to mode 600 when the file is created
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        File rv = File.createTempFile(prefix, suffix);
        // same thing as below but static
        SecureFileOutputStream.setPerms(rv);
        return rv;
    }

    /**
     *  Sets file to mode 600 when the file is created
     */
    public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
        File rv = File.createTempFile(prefix, suffix, directory);
        // same thing as below but static
        SecureFileOutputStream.setPerms(rv);
        return rv;
    }

    /**
     *  Tries to set the permissions to 600,
     *  ignores errors
     */
    protected void setPerms() {
        if (!SecureFileOutputStream.canSetPerms())
            return;
        try {
            setReadable(false, false);
            setReadable(true, true);
            setWritable(false, false);
            setWritable(true, true);
            if (isNotWindows && isDirectory()) {
                setExecutable(false, false);
                setExecutable(true, true);
            }
        } catch (Throwable t) {
            // NoSuchMethodException or NoSuchMethodError if we somehow got the
            // version detection wrong or the JVM doesn't support it
        }
    }
}
