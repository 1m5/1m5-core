package io.onemfive.core.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashUtil {

    public static String generateHash(String contentToHash, String hashAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int size = 128;
        int cost = 16;
        int iterations = 1 << cost;
        byte[] salt = new byte[size/8];
        new SecureRandom().nextBytes(salt);
        KeySpec spec = new PBEKeySpec(contentToHash.toCharArray(), salt, iterations, size);
        SecretKeyFactory f = SecretKeyFactory.getInstance(hashAlgorithm);
        byte[] dk = f.generateSecret(spec).getEncoded();
        byte[] hash = new byte[salt.length + dk.length];
        System.arraycopy(salt, 0, hash, 0, salt.length);
        System.arraycopy(dk, 0, hash, salt.length, dk.length);
//        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
//        return "$31$" + cost + '$' + enc.encodeToString(hash);
        return "$31$" + cost + '$' + io.onemfive.core.util.data.Base64.encode(hash);
    }

    public static boolean verifyHash(String hashToVerify, String hashAlgorithm, String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int size = 128;
        int cost = 16;
        int iterations = 1 << cost;
        final Pattern layout = Pattern.compile("\\$31\\$(\\d\\d?)\\$(.{43})");
        Matcher m = layout.matcher(token);
        byte[] hash = io.onemfive.core.util.data.Base64.decode(m.group(2));
//        byte[] hash = Base64.getUrlDecoder().decode(m.group(2));
        byte[] salt = Arrays.copyOfRange(hash, 0, size / 8);
        KeySpec spec = new PBEKeySpec(hashToVerify.toCharArray(), salt, iterations, size);
        SecretKeyFactory f = SecretKeyFactory.getInstance(hashAlgorithm);
        byte[] check = f.generateSecret(spec).getEncoded();
        int zero = 0;
        for (int idx = 0; idx < check.length; ++idx)
            zero |= hash[salt.length + idx] ^ check[idx];
        return zero == 0;
    }

}
