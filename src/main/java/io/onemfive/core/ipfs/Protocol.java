package io.onemfive.core.ipfs;

import io.onemfive.core.util.Base32;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.HashMap;
import java.util.Map;

public class Protocol {

    enum Type {
        IP4(4, 32, "ip4"),
        TCP(6, 16, "tcp"),
        UDP(17, 16, "udp"),
        DCCP(33, 16, "dccp"),
        IP6(41, 128, "ip6"),
        SCTP(132, 16, "sctp"),
        UTP(301, 0, "utp"),
        UDT(302, 0, "udt"),
        IPFS(421, Protocol.LENGTH_PREFIXED_VAR_SIZE, "ipfs"),
        HTTPS(443, 0, "https"),
        HTTP(480, 0, "http"),
        ONION(444, 80, "onion");

        public final int code;
        public final int size;
        public final String name;
        private final byte[] encoded;

        Type(int code, int size, String name) {
            this.code = code;
            this.size = size;
            this.name = name;
            this.encoded = encode(code);
        }

        static byte[] encode(int code) {
            byte[] varint = new byte[(32 - Integer.numberOfLeadingZeros(code) + 6) / 7];
            Protocol.putUvarint(varint, (long)code);
            return varint;
        }
    }

    public static int LENGTH_PREFIXED_VAR_SIZE = -1;
    public final Protocol.Type type;
    private static Map<String, Protocol> byName = new HashMap<>();
    private static Map<Integer, Protocol> byCode = new HashMap<>();

    public Protocol(Protocol.Type type) {
        this.type = type;
    }

    public void appendCode(OutputStream out) throws IOException {
        out.write(this.type.encoded);
    }

    public int size() {
        return type.size;
    }

    public String name() {
        return type.name;
    }

    public int code() {
        return type.code;
    }

    public String toString() {
        return name();
    }

    public byte[] addressToBytes(String addr) {
        try {
            switch(type.ordinal()) {
                case 1:
                    return Inet4Address.getByName(addr).getAddress();
                case 2:
                    return Inet6Address.getByName(addr).getAddress();
                case 3:
                case 4:
                case 5:
                case 6:
                    int e = Integer.parseInt(addr);
                    if(e > '\uffff') {
                        throw new IllegalStateException("Failed to parse " + this.type.name + " address " + addr + " (> 65535");
                    }

                    return new byte[]{(byte)(e >> 8), (byte)e};
                case 7:
                    Multihash hash = Multihash.fromBase58(addr);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] hashBytes = hash.toBytes();
                    byte[] varint = new byte[(32 - Integer.numberOfLeadingZeros(hashBytes.length) + 6) / 7];
                    putUvarint(varint, (long)hashBytes.length);
                    bout.write(varint);
                    bout.write(hashBytes);
                    return bout.toByteArray();
                case 8:
                    String[] split = addr.split(":");
                    if(split.length != 2) {
                        throw new IllegalStateException("Onion address needs a port: " + addr);
                    }

                    if(split[0].length() != 16) {
                        throw new IllegalStateException("failed to parse " + this.name() + " addr: " + addr + " not a Tor onion address.");
                    }

                    byte[] onionHostBytes = Base32.decode(split[0].toUpperCase());
                    int port = Integer.parseInt(split[1]);
                    if(port > '\uffff') {
                        throw new IllegalStateException("Port is > 65535: " + port);
                    }

                    if(port < 1) {
                        throw new IllegalStateException("Port is < 1: " + port);
                    }

                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream dout = new DataOutputStream(b);
                    dout.write(onionHostBytes);
                    dout.writeShort(port);
                    dout.flush();
                    return b.toByteArray();
            }
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }

        throw new IllegalStateException("Failed to parse address: " + addr);
    }

    public String readAddress(InputStream in) throws IOException {
        int sizeForAddress = this.sizeForAddress(in);
        byte[] buf;
        switch(type.ordinal()) {
            case 1:
                buf = new byte[sizeForAddress];
                in.read(buf);
                return Inet4Address.getByAddress(buf).toString().substring(1);
            case 2:
                buf = new byte[sizeForAddress];
                in.read(buf);
                return Inet6Address.getByAddress(buf).toString().substring(1);
            case 3:
            case 4:
            case 5:
            case 6:
                return Integer.toString(in.read() << 8 | in.read());
            case 7:
                buf = new byte[sizeForAddress];
                in.read(buf);
                return (new Multihash(buf)).toBase58();
            case 8:
                byte[] host = new byte[10];
                in.read(host);
                String port = Integer.toString(in.read() << 8 | in.read());
                return Base32.encode(host) + ":" + port;
            default:
                throw new IllegalStateException("Unimplemented protocl type: " + this.type.name);
        }
    }

    public int sizeForAddress(InputStream in) throws IOException {
        return this.type.size > 0?this.type.size / 8:(this.type.size == 0?0:(int)readVarint(in));
    }

    static int putUvarint(byte[] buf, long x) {
        int i;
        for(i = 0; x >= 128L; ++i) {
            buf[i] = (byte)((int)(x | 128L));
            x >>= 7;
        }

        buf[i] = (byte)((int)x);
        return i + 1;
    }

    static long readVarint(InputStream in) throws IOException {
        long x = 0L;
        int s = 0;

        for(int i = 0; i < 10; ++i) {
            int b = in.read();
            if(b == -1) {
                throw new EOFException();
            }

            if(b < 128) {
                if(i <= 9 && (i != 9 || b <= 1)) {
                    return x | (long)b << s;
                }

                throw new IllegalStateException("Overflow reading varint" + -(i + 1));
            }

            x |= ((long)b & 127L) << s;
            s += 7;
        }

        throw new IllegalStateException("Varint too long!");
    }

    public static Protocol get(String name) {
        if(byName.containsKey(name)) {
            return byName.get(name);
        } else {
            throw new IllegalStateException("No protocol with name: " + name);
        }
    }

    public static Protocol get(int code) {
        if(byCode.containsKey(Integer.valueOf(code))) {
            return byCode.get(Integer.valueOf(code));
        } else {
            throw new IllegalStateException("No protocol with code: " + code);
        }
    }

    static {
        Protocol.Type[] var0 = Protocol.Type.values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            Protocol.Type t = var0[var2];
            Protocol p = new Protocol(t);
            byName.put(p.name(), p);
            byCode.put(Integer.valueOf(p.code()), p);
        }

    }
}