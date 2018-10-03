package io.onemfive.core;

import io.onemfive.data.util.Base32;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    private Util() { }
    
    /** Reads all data from an <code>InputStream</code> */
    public static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[32*1024];
        int bytesRead;
        do {
            bytesRead = inputStream.read(buffer, 0, buffer.length);
            if (bytesRead > 0)
                byteStream.write(buffer, 0, bytesRead);
        } while (bytesRead > 0);
        return byteStream.toByteArray();
    }
    
    /**
     * Opens a <code>URL</code> and reads one line at a time.
     * Returns the lines as a <code>List</code> of <code>String</code>s,
     * or an empty <code>List</code> if an error occurred.
     * @param url
     * @see #readLines(File)
     */
    public static List<String> readLines(URL url) {
        LOG.info("Reading URL: <" + url + ">");
        
        InputStream stream = null;
        try {
            stream = url.openStream();
            return readLines(stream);
        }
        catch (IOException e) {
            LOG.warning("Error reading URL.");
            return Collections.emptyList();
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warning("Can't close input stream.");
                }
        }
    }
    
    /**
     * Opens a <code>File</code> and reads one line at a time.
     * Returns the lines as a <code>List</code> of <code>String</code>s,
     * or an empty <code>List</code> if an error occurred.
     * @param file
     * @see #readLines(URL)
     */
    public static List<String> readLines(File file) {
        LOG.info("Reading file: <" + file.getAbsolutePath() + ">");
        
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            return readLines(stream);
        } catch (IOException e) {
            LOG.warning("Error reading file.");
            return Collections.emptyList();
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warning("Can't close input stream.");
                }
        }
    }

    public static List<String> readLines(String fileName) throws IOException {
        File f = new File(fileName);
        LOG.info("Loading lines in file "+f.getAbsolutePath()+"...");
        return Files.readAllLines(f.toPath(), Charset.defaultCharset());
    }
    
    /**
     * Opens an <code>InputStream</code> and reads one line at a time.
     * Returns the lines as a <code>List</code> of <code>String</code>s.
     * or an empty <code>List</code> if an error occurred.
     * @param inputStream
     * @see #readLines(URL)
     */
    public static List<String> readLines(InputStream inputStream) throws IOException {
        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = new ArrayList<String>();
        
        while (true) {
            String line = null;
            line = inputBuffer.readLine();
            if (line == null)
                break;
            lines.add(line);
        }
        
        LOG.info(lines.size() + " lines read.");
        return lines;
    }
    
    /**
     * Reads all data from an input stream and writes it to an output stream.
     * @param input
     * @param output
     * @throws IOException
     */
    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024*1024];
        int bytesRead;
        while ((bytesRead=input.read(buffer)) >= 0)
            output.write(buffer, 0, bytesRead);
    }
    
    public static void copy(File from, File to) throws IOException {
        if (!to.exists())
            to.createNewFile();
    
        FileChannel fromChan = null;
        FileChannel toChan = null;
        try {
            fromChan = new FileInputStream(from).getChannel();
            toChan = new FileOutputStream(to).getChannel();
            toChan.transferFrom(fromChan, 0, fromChan.size());
        }
        finally {
            if (fromChan != null)
                fromChan.close();
            if (toChan != null)
                toChan.close();
            
            // This is needed on Windows so a file can be deleted after copying it.
            System.gc();
        }
    }

    /**
     * Tests if a directory contains a file with a given name.
     * @param directory
     * @param filename
     */
    public static boolean contains(File directory, final String filename) {
        String[] matches = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(filename);
            }
        });
        
        return matches!=null && matches.length>0;
    }

    
    /** Returns a <code>ThreadFactory</code> that creates threads that run at minimum priority */
    public static ThreadFactory createThreadFactory(final String threadName, final int stackSize) {
        return createThreadFactory(threadName, stackSize, Thread.MIN_PRIORITY);
    }
    
    public static ThreadFactory createThreadFactory(final String threadName, final int stackSize, final int priority) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread newThread = new Thread(Thread.currentThread().getThreadGroup(), runnable, threadName, stackSize);
                newThread.setPriority(priority);
                return newThread;
            }
        };
    }
    
    /**
     * Creates a thread-safe <code>Iterable</code> from a thread-unsafe one.
     * Modifications to the old <code>Iterable</code> will not affect the
     * new one.
     * @param <E>
     * @param iterable
     */
    public static <E> Iterable<E> synchronizedCopy(Iterable<E> iterable) {
        synchronized(iterable) {
            Collection<E> collection = new ArrayList<E>();
            for (E element: iterable)
                collection.add(element);
            return collection;
        }
    }
    
    /**
     * Removes all whitespace at the beginning and the end of a string,
     * and replaces multiple whitespace characters with a single space.
     * @param string
     */
    public static String removeExtraWhitespace(String string) {
        if (string == null)
            return null;
        return string.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Removes whitespace from the beginning and end of an address.
     * Also removes angle brackets if the address begins and ends
     * with an angle bracket.
     * @param address
     */
    public static String fixAddress(String address) {
        if (address == null)
            return null;
        
        address = address.trim();
        if (address.startsWith("<") && address.endsWith(">"))
            address = address.substring(1, address.length()-1);
        
        return address;
    }
    
    /** Overwrites a <code>byte</code> array with zeros */
    public static void zeroOut(byte[] array) {
        for (int i=0; i<array.length; i++)
            array[i] = 0;
    }
    
    /** Overwrites a <code>char</code> array with zeros */
    public static void zeroOut(char[] array) {
        for (int i=0; i<array.length; i++)
            array[i] = 0;
    }
    
    public static byte[] concat(byte[] arr1, byte[]arr2) {
        byte[] arr3 = Arrays.copyOf(arr1, arr1.length + arr2.length);
        System.arraycopy(arr2, 0, arr3, arr1.length, arr2.length);
        return arr3;
    }

    /** Returns the MIME type of the picture, for example <code>image/jpeg</code>. */
    public static String getPictureType(byte[] picture) {
        ByteArrayInputStream stream = new ByteArrayInputStream(picture);
        try {
            return URLConnection.guessContentTypeFromStream(stream);
        } catch (IOException e) {
            LOG.warning("Can't read from ByteArrayInputStream");
            return null;
        }
    }

    public static boolean getBooleanParameter(Properties properties, String parameterName, boolean defaultValue) {
        String stringValue = properties.getProperty(parameterName);
        if ("true".equalsIgnoreCase(stringValue) || "yes".equalsIgnoreCase(stringValue) || "on".equalsIgnoreCase(stringValue) || "1".equals(stringValue))
            return true;
        else if ("false".equalsIgnoreCase(stringValue) || "no".equalsIgnoreCase(stringValue) || "off".equalsIgnoreCase(stringValue) || "0".equals(stringValue))
            return false;
        else if (stringValue == null)
            return defaultValue;
        else
            throw new IllegalArgumentException("<" + stringValue + "> is not a legal value for the boolean parameter <" + parameterName + ">");
    }

    public static int getIntParameter(Properties properties, String parameterName, int defaultValue) {
        String stringValue = properties.getProperty(parameterName);
        if (stringValue == null)
            return defaultValue;
        else
            try {
                return new Integer(stringValue);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Can't convert value <" + stringValue + "> for parameter <" + parameterName + "> to int.");
            }
    }
}
