package io.onemfive.core.ipfs;

import io.onemfive.core.util.Base58;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Multihash {
    public final Multihash.Type type;
    private final byte[] hash;

    public Multihash(Multihash.Type type, byte[] hash) {
        if(hash.length > 127) {
            throw new IllegalStateException("Unsupported hash size: " + hash.length);
        } else if(hash.length != type.length) {
            throw new IllegalStateException("Incorrect hash length: " + hash.length + " != " + type.length);
        } else {
            this.type = type;
            this.hash = hash;
        }
    }

    public Multihash(byte[] multihash) {
        this(Multihash.Type.lookup(multihash[0] & 255), Arrays.copyOfRange(multihash, 2, multihash.length));
    }

    public byte[] toBytes() {
        byte[] res = new byte[hash.length + 2];
        res[0] = (byte)type.index;
        res[1] = (byte)hash.length;
        System.arraycopy(hash, 0, res, 2, hash.length);
        return res;
    }

    public void serialize(DataOutput dout) throws IOException {
        dout.write(this.toBytes());
    }

    public static Multihash deserialize(DataInput din) throws IOException {
        int type = din.readUnsignedByte();
        int len = din.readUnsignedByte();
        Multihash.Type t = Multihash.Type.lookup(type);
        byte[] hash = new byte[len];
        din.readFully(hash);
        return new Multihash(t, hash);
    }

    public String toString() {
        return this.toBase58();
    }

    public boolean equals(Object o) {
        return !(o instanceof Multihash)?false:type == ((Multihash)o).type && Arrays.equals(hash, ((Multihash)o).hash);
    }

    public int hashCode() {
        return Arrays.hashCode(hash) ^ type.hashCode();
    }

    public String toHex() {
        StringBuilder res = new StringBuilder();
        byte[] var2 = this.toBytes();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            res.append(String.format("%x", new Object[]{Integer.valueOf(b & 255)}));
        }

        return res.toString();
    }

    public String toBase58() {
        return Base58.encode(this.toBytes());
    }

    public static Multihash fromHex(String hex) {
        if(hex.length() % 2 != 0) {
            throw new IllegalStateException("Uneven number of hex digits!");
        } else {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            for(int i = 0; i < hex.length() - 1; i += 2) {
                bout.write(Integer.valueOf(hex.substring(i, i + 2), 16).intValue());
            }

            return new Multihash(bout.toByteArray());
        }
    }

    public static Multihash fromBase58(String base58) {
        return new Multihash(Base58.decode(base58));
    }

    public enum Type {
        sha1(17, 20),
        sha2_256(18, 32),
        sha2_512(19, 64),
        sha3(20, 64),
        blake2b(64, 64),
        blake2s(65, 32);

        public int index;
        public int length;
        private static Map<Integer, Multihash.Type> lookup;

        Type(int index, int length) {
            this.index = index;
            this.length = length;
        }

        public static Multihash.Type lookup(int t) {
            if(!lookup.containsKey(t)) {
                throw new IllegalStateException("Unknown Multihash type: " + t);
            } else {
                return lookup.get(t);
            }
        }

        static {
            lookup = new TreeMap<>();
            Multihash.Type[] var0 = values();
            int var1 = var0.length;

            for(int var2 = 0; var2 < var1; ++var2) {
                Multihash.Type t = var0[var2];
                lookup.put(t.index, t);
            }

        }
    }
}
