package io.onemfive.core.util;

import io.onemfive.core.util.data.Base64;
import io.onemfive.data.DID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashUtil {

    public static String generateHash(String contentToHash) {
        int iterations = 1000;
        byte[] salt = new byte[16];
        byte[] hash;
        try {
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(contentToHash.toCharArray(), salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hash = skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String hashString = iterations + ":" + toHex(salt) + ":" + toHex(hash);
        return Base64.encode(hashString);
    }

    public static Boolean verifyHash(String contentToVerify, String hashToVerify) {
        String hashString = Base64.decodeToString(hashToVerify);
        String[] parts = hashString.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(contentToVerify.toCharArray(), salt, iterations, hash.length * 8);
        byte[] testHash;
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            testHash = skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    private static byte[] fromHex(String hex)
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

//    public static void main(String[] args) {
//        DID did = new DID();
//        did.setAlias("Alice");
//        String hash = HashUtil.generateHash(did.getAlias());
//        System.out.println("Alias Hash: "+hash);
//        Boolean aliasVerified = HashUtil.verifyHash("Alice", hash);
//        System.out.println("Alias Verified: "+aliasVerified);
//    }

}
