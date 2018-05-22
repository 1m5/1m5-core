package io.onemfive.core.ipfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

public class MultiAddress {
    private final byte[] raw;

    public MultiAddress(String address) {
        this(decodeFromString(address));
    }

    public MultiAddress(byte[] raw) {
        encodeToString(raw);
        this.raw = raw;
    }

    public byte[] getBytes() {
        return Arrays.copyOfRange(this.raw, 0, this.raw.length);
    }

    public boolean isTCPIP() {
        String[] parts = this.toString().substring(1).split("/");
        return parts.length != 4?false:(!parts[0].startsWith("ip")?false:parts[2].equals("tcp"));
    }

    public String getHost() {
        String[] parts = this.toString().substring(1).split("/");
        if(parts[0].startsWith("ip")) {
            return parts[1];
        } else {
            throw new IllegalStateException("This multiaddress doesn\'t have a host: " + this.toString());
        }
    }

    public int getTCPPort() {
        String[] parts = this.toString().substring(1).split("/");
        if(parts[2].startsWith("tcp")) {
            return Integer.parseInt(parts[3]);
        } else {
            throw new IllegalStateException("This multiaddress doesn\'t have a tcp port: " + this.toString());
        }
    }

    private static byte[] decodeFromString(String addr) {
        while(addr.endsWith("/")) {
            addr = addr.substring(0, addr.length() - 1);
        }

        String[] parts = addr.split("/");
        if(parts[0].length() != 0) {
            throw new IllegalStateException("MultiAddress must start with a /");
        } else {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            try {
                int e = 1;

                while(e < parts.length) {
                    String part = parts[e++];
                    Protocol p = Protocol.get(part);
                    p.appendCode(bout);
                    if(p.size() != 0) {
                        String component = parts[e++];
                        if(component.length() == 0) {
                            throw new IllegalStateException("Protocol requires address, but non provided!");
                        }

                        bout.write(p.addressToBytes(component));
                    }
                }

                return bout.toByteArray();
            } catch (IOException var7) {
                throw new IllegalStateException("Error decoding multiaddress: " + addr);
            }
        }
    }

    private static String encodeToString(byte[] raw) {
        StringBuilder b = new StringBuilder();
        ByteArrayInputStream in = new ByteArrayInputStream(raw);

        try {
            while(true) {
                while(true) {
                    int e = (int)Protocol.readVarint(in);
                    Protocol p = Protocol.get(e);
                    b.append("/" + p.name());
                    if(p.size() != 0) {
                        String addr = p.readAddress(in);
                        if(addr.length() > 0) {
                            b.append("/" + addr);
                        }
                    }
                }
            }
        } catch (EOFException var6) {
            return b.toString();
        } catch (IOException var7) {
            throw new RuntimeException(var7);
        }
    }

    public String toString() {
        return encodeToString(this.raw);
    }

    public boolean equals(Object other) {
        return !(other instanceof MultiAddress)?false:Arrays.equals(this.raw, ((MultiAddress)other).raw);
    }

    public int hashCode() {
        return Arrays.hashCode(this.raw);
    }
}
