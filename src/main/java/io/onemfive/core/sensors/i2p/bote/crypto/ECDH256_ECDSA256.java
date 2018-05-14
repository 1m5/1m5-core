package io.onemfive.core.sensors.i2p.bote.crypto;

import net.i2p.data.Base64;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class ECDH256_ECDSA256 extends ECDH_ECDSA {

    public ECDH256_ECDSA256() throws GeneralSecurityException {
        super("P-256", "SHA256withECDSA", 33);   // Use the NIST P-256 curve, also known as secp256r1
    }

    @Override
    public String getName() {
        return "ECDH-256 / ECDSA-256";
    }

    @Override
    public byte getId() {
        return 2;
    }

    @Override
    public int getBase64PublicKeyPairLength() {
        return 86;
    }

    @Override
    public int getBase64CompleteKeySetLength() {
        return 172;
    }

    @Override
    public String getBase64InitialCharacters() {
        return "ghijklmnopqrstuvwxyz0123456789";
    }

    @Override
    protected byte[] toByteArray(PublicKey key) {
        ECPublicKey ecKey = castToEcKey(key);
        return ECUtils.encodePoint(ecKey.getParams(), ecKey.getW(), true);
    }

    @Override
    protected ECPublicKeySpec createPublicKeySpec(byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        // decompress into an EC point
        ECPoint w = ECUtils.decodePoint(ecParameterSpec.getCurve(), encodedKey);

        // make a public key from the public point w
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(w, ecParameterSpec);

        return publicKeySpec;
    }

    @Override
    public PrivateKeyPair createPrivateKeyPair(String base64) throws GeneralSecurityException {
        int base64PrivateKeyLength = getBase64PrivateKeyPairLength() / 2;
        String base64EncrKey = "A" + base64.substring(0, base64PrivateKeyLength);
        byte[] encrKeyBytes = Base64.decode(base64EncrKey);
        String base64SigKey = "A" + base64.substring(base64PrivateKeyLength);
        byte[] sigKeyBytes = Base64.decode(base64SigKey);

        byte[] bytes = Arrays.copyOf(encrKeyBytes, encrKeyBytes.length + sigKeyBytes.length);
        System.arraycopy(sigKeyBytes, 0, bytes, encrKeyBytes.length, sigKeyBytes.length);
        return createPrivateKeyPair(bytes);
    }
}
