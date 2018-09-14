package io.onemfive.core.util;

import io.onemfive.core.util.data.DataHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

public class HashCash implements Comparable<HashCash> {
    public static final int DefaultVersion = 1;
    private static final int hashLength = 160;
    private static final String dateFormatString = "yyMMdd";
    private static long milliFor16 = -1L;
    private String myToken;
    private int myValue;
    private Calendar myDate;
    private Map<String, List<String>> myExtensions;
    private int myVersion;
    private String myResource;

    public HashCash(String cash) throws NoSuchAlgorithmException {
        this.myToken = cash;
        String[] parts = cash.split(":");
        this.myVersion = Integer.parseInt(parts[0]);
        if (this.myVersion >= 0 && this.myVersion <= 1) {
            if (this.myVersion == 0 && parts.length != 6 || this.myVersion == 1 && parts.length != 7) {
                throw new IllegalArgumentException("Improperly formed HashCash");
            } else {
                try {
                    int index = 1;
                    if (this.myVersion == 1) {
                        this.myValue = Integer.parseInt(parts[index++]);
                    } else {
                        this.myValue = 0;
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
                    Calendar tempCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    tempCal.setTime(dateFormat.parse(parts[index++]));
                    this.myResource = parts[index++];
                    this.myExtensions = deserializeExtensions(parts[index++]);
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    md.update(DataHelper.getUTF8(cash));
                    byte[] tempBytes = md.digest();
                    int tempValue = numberOfLeadingZeros(tempBytes);
                    if (this.myVersion == 0) {
                        this.myValue = tempValue;
                    } else if (this.myVersion == 1) {
                        this.myValue = tempValue > this.myValue ? this.myValue : tempValue;
                    }

                } catch (ParseException var9) {
                    throw new IllegalArgumentException("Improperly formed HashCash", var9);
                }
            }
        } else {
            throw new IllegalArgumentException("Only supported versions are 0 and 1");
        }
    }

    private HashCash() throws NoSuchAlgorithmException {
    }

    public static HashCash mintCash(String resource, int value) throws NoSuchAlgorithmException {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return mintCash(resource, (Map)null, now, value, 1);
    }

    public static HashCash mintCash(String resource, int value, int version) throws NoSuchAlgorithmException {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return mintCash(resource, (Map)null, now, value, version);
    }

    public static HashCash mintCash(String resource, Calendar date, int value) throws NoSuchAlgorithmException {
        return mintCash(resource, (Map)null, date, value, 1);
    }

    public static HashCash mintCash(String resource, Calendar date, int value, int version) throws NoSuchAlgorithmException {
        return mintCash(resource, (Map)null, date, value, version);
    }

    public static HashCash mintCash(String resource, Map<String, List<String>> extensions, int value) throws NoSuchAlgorithmException {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return mintCash(resource, extensions, now, value, 1);
    }

    public static HashCash mintCash(String resource, Map<String, List<String>> extensions, int value, int version) throws NoSuchAlgorithmException {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return mintCash(resource, extensions, now, value, version);
    }

    public static HashCash mintCash(String resource, Map<String, List<String>> extensions, Calendar date, int value) throws NoSuchAlgorithmException {
        return mintCash(resource, extensions, date, value, 1);
    }

    public static HashCash mintCash(String resource, Map<String, List<String>> extensions, Calendar date, int value, int version) throws NoSuchAlgorithmException {
        if (version >= 0 && version <= 1) {
            if (value >= 0 && value <= 160) {
                if (resource.contains(":")) {
                    throw new IllegalArgumentException("Resource may not contain a colon.");
                } else {
                    HashCash result = new HashCash();
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    result.myResource = resource;
                    result.myExtensions = (Map)(null == extensions ? new HashMap() : extensions);
                    result.myDate = date;
                    result.myVersion = version;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
                    String prefix;
                    switch(version) {
                        case 0:
                            prefix = version + ":" + dateFormat.format(date.getTime()) + ":" + resource + ":" + serializeExtensions(extensions) + ":";
                            result.myToken = generateCash(prefix, value, md);
                            md.reset();
                            md.update(DataHelper.getUTF8(result.myToken));
                            result.myValue = numberOfLeadingZeros(md.digest());
                            break;
                        case 1:
                            result.myValue = value;
                            prefix = version + ":" + value + ":" + dateFormat.format(date.getTime()) + ":" + resource + ":" + serializeExtensions(extensions) + ":";
                            result.myToken = generateCash(prefix, value, md);
                            break;
                        default:
                            throw new IllegalArgumentException("Only supported versions are 0 and 1");
                    }

                    return result;
                }
            } else {
                throw new IllegalArgumentException("Value must be between 0 and 160");
            }
        } else {
            throw new IllegalArgumentException("Only supported versions are 0 and 1");
        }
    }

    public boolean equals(Object obj) {
        return obj instanceof HashCash ? this.toString().equals(obj.toString()) : super.equals(obj);
    }

    public int hashCode() {
        return ("HashCash:" + this.toString()).hashCode();
    }

    public String toString() {
        return this.myToken;
    }

    public Map<String, List<String>> getExtensions() {
        return this.myExtensions;
    }

    public String getResource() {
        return this.myResource;
    }

    public Calendar getDate() {
        return this.myDate;
    }

    public int getValue() {
        return this.myValue;
    }

    public int getVersion() {
        return this.myVersion;
    }

    private static String generateCash(String prefix, int value, MessageDigest md) throws NoSuchAlgorithmException {
        SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        byte[] tmpBytes = new byte[8];
        rnd.nextBytes(tmpBytes);
        long random = bytesToLong(tmpBytes);
        rnd.nextBytes(tmpBytes);
        long counter = bytesToLong(tmpBytes);
        prefix = prefix + Long.toHexString(random) + ":";

        String temp;
        int tempValue;
        do {
            ++counter;
            temp = prefix + Long.toHexString(counter);
            md.reset();
            md.update(DataHelper.getUTF8(temp));
            byte[] bArray = md.digest();
            tempValue = numberOfLeadingZeros(bArray);
        } while(tempValue < value);

        return temp;
    }

    private static long bytesToLong(byte[] b) {
        long l = 0L;
        l |= (long)(b[0] & 255);
        l <<= 8;
        l |= (long)(b[1] & 255);
        l <<= 8;
        l |= (long)(b[2] & 255);
        l <<= 8;
        l |= (long)(b[3] & 255);
        l <<= 8;
        l |= (long)(b[4] & 255);
        l <<= 8;
        l |= (long)(b[5] & 255);
        l <<= 8;
        l |= (long)(b[6] & 255);
        l <<= 8;
        l |= (long)(b[7] & 255);
        return l;
    }

    private static String serializeExtensions(Map<String, List<String>> extensions) {
        if (null != extensions && !extensions.isEmpty()) {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            Iterator var4 = extensions.entrySet().iterator();

            label52:
            while(var4.hasNext()) {
                Entry<String, List<String>> entry = (Entry)var4.next();
                String key = (String)entry.getKey();
                if (!key.contains(":") && !key.contains(";") && !key.contains("=")) {
                    if (!first) {
                        result.append(";");
                    }

                    first = false;
                    result.append(key);
                    List<String> tempList = (List)entry.getValue();
                    if (null == tempList) {
                        continue;
                    }

                    result.append("=");
                    int i = 0;

                    while(true) {
                        if (i >= tempList.size()) {
                            continue label52;
                        }

                        if (((String)tempList.get(i)).contains(":") || ((String)tempList.get(i)).contains(";") || ((String)tempList.get(i)).contains(",")) {
                            throw new IllegalArgumentException("Extension value contains an illegal character. " + (String)tempList.get(i));
                        }

                        if (i > 0) {
                            result.append(",");
                        }

                        result.append((String)tempList.get(i));
                        ++i;
                    }
                }

                throw new IllegalArgumentException("Extension key contains an illegal character. " + key);
            }

            return result.toString();
        } else {
            return "";
        }
    }

    private static Map<String, List<String>> deserializeExtensions(String extensions) {
        Map<String, List<String>> result = new HashMap();
        if (null != extensions && extensions.length() != 0) {
            String[] items = extensions.split(";");

            for(int i = 0; i < items.length; ++i) {
                String[] parts = items[i].split("=", 2);
                if (parts.length == 1) {
                    result.put(parts[0], null);
                } else {
                    result.put(parts[0], Arrays.asList(parts[1].split(",")));
                }
            }

            return result;
        } else {
            return result;
        }
    }

    private static int numberOfLeadingZeros(byte[] values) {
        int result = 0;
        int temp = 0;

        for(int i = 0; i < values.length; ++i) {
            temp = numberOfLeadingZeros(values[i]);
            result += temp;
            if (temp != 8) {
                break;
            }
        }

        return result;
    }

    private static int numberOfLeadingZeros(byte value) {
        if (value < 0) {
            return 0;
        } else if (value < 1) {
            return 8;
        } else if (value < 2) {
            return 7;
        } else if (value < 4) {
            return 6;
        } else if (value < 8) {
            return 5;
        } else if (value < 16) {
            return 4;
        } else if (value < 32) {
            return 3;
        } else if (value < 64) {
            return 2;
        } else {
            return value < 128 ? 1 : 0;
        }
    }

    public static long estimateTime(int value) throws NoSuchAlgorithmException {
        initEstimates();
        return (long)((double)milliFor16 * Math.pow(2.0D, (double)(value - 16)));
    }

    public static int estimateValue(int secs) throws NoSuchAlgorithmException {
        initEstimates();
        int result = 0;
        long millis = (long)secs * 1000L * 65536L;

        for(millis /= milliFor16; millis > 1L; millis /= 2L) {
            ++result;
        }

        return result;
    }

    private static void initEstimates() throws NoSuchAlgorithmException {
        if (milliFor16 == -1L) {
            long duration = Calendar.getInstance().getTimeInMillis();

            for(int i = 0; i < 11; ++i) {
                mintCash("estimation", 16);
            }

            duration = Calendar.getInstance().getTimeInMillis() - duration;
            milliFor16 = duration / 10L;
        }

    }

    public int compareTo(HashCash other) {
        if (null == other) {
            throw new NullPointerException();
        } else {
            return Integer.valueOf(this.getValue()).compareTo(other.getValue());
        }
    }
}
