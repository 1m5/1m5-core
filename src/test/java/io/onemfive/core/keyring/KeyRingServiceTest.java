package io.onemfive.core.keyring;

import io.onemfive.core.Service;
import io.onemfive.core.ServiceRequest;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.FileUtil;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class KeyRingServiceTest {

    KeyRingService service;

    String passphrase = "1234";
    String aliasBob = "Bob";
    String aliasCharlie = "Charlie";

    @Before
    public void init() {

        service = new KeyRingService(null, null);
        service.start(null);

        // Generate Key Ring Collections
        if(!FileUtil.fileExists(aliasBob+".pkr")) {
            FileUtil.rmFile(aliasBob+".skr");
            GenerateKeyRingCollectionsRequest lr = new GenerateKeyRingCollectionsRequest();
            lr.keyRingUsername = aliasBob;
            lr.keyRingPassphrase = passphrase;
            Envelope e = Envelope.documentFactory();
            DLC.addData(GenerateKeyRingCollectionsRequest.class, lr, e);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_GENERATE_KEY_RINGS_COLLECTIONS, e);
            e.setRoute(e.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
            service.handleDocument(e);
        }
        if(!FileUtil.fileExists(aliasCharlie+".pkr")) {
            FileUtil.rmFile(aliasCharlie + ".skr");
            GenerateKeyRingCollectionsRequest lr2 = new GenerateKeyRingCollectionsRequest();
            lr2.keyRingUsername = aliasCharlie;
            lr2.keyRingPassphrase = passphrase;
            Envelope e2 = Envelope.documentFactory();
            DLC.addData(GenerateKeyRingCollectionsRequest.class, lr2, e2);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_GENERATE_KEY_RINGS_COLLECTIONS, e2);
            e2.setRoute(e2.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
            service.handleDocument(e2);
        }
    }

    @After
    public void teardown() {
        service.shutdown();
    }

//    @Test
//    public void genKeyRingCollection() {
//
//        String aliasAlice = "Annie";
//        String passphrase = "1234";
//
//        // Generate Key Ring Collections
//        GenerateKeyRingCollectionsRequest lr = new GenerateKeyRingCollectionsRequest();
//        lr.keyRingUsername = aliasAlice;
//        lr.keyRingPassphrase = passphrase;
//        Envelope e = Envelope.documentFactory();
//        DLC.addData(GenerateKeyRingCollectionsRequest.class, lr, e);
//        DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_GENERATE_KEY_RINGS_COLLECTIONS, e);
//        e.setRoute(e.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
//
//        long start = new Date().getTime();
//        service.handleDocument(e);
//        long end = new Date().getTime();
//        long duration = end - start;
//        System.out.println("Generate Key Ring Duration (ms): " + duration);
//        assert FileUtil.fileExists("Annie.pkr");
//        assert FileUtil.fileExists("Annie.skr");
//
//        FileUtil.rmFile("Annie.pkr");
//        FileUtil.rmFile("Annie.skr");
//    }

//    @Test
//    public void authN() {
//
//        AuthNRequest r = new AuthNRequest();
//        r.keyRingUsername = aliasBob;
//        r.keyRingPassphrase = passphrase;
//        r.alias = aliasBob;
//        r.aliasPassphrase = passphrase;
//        r.autoGenerate = true;
//
//        Envelope e = Envelope.documentFactory();
//        DLC.addData(AuthNRequest.class, r, e);
//        DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_AUTHN, e);
//        e.setRoute(e.getDynamicRoutingSlip().nextRoute());
//        long start = new Date().getTime();
//        service.handleDocument(e);
//        long end = new Date().getTime();
//        long duration = end - start;
//        System.out.println("Get Public Key Duration: " + duration);
//        assert r.publicKey != null && r.publicKey.getAddress() != null;
//        System.out.println("Public Key Fingerprint: " + r.publicKey.getFingerprint());
//        System.out.println("Public Key Address: " + r.publicKey.getAddress());
//        System.out.println("Public Key Is Identity Key: " + r.publicKey.isIdentityKey());
//        System.out.println("Public Key is Encryption Key: " + r.publicKey.isEncryptionKey());
//    }

    @Test
    public void cryption() {
        String passphrase = "1234";
        String plaintext = "This is a secret";
        System.out.println("Content to encrypt: \n"+plaintext);
        try {
            // Encrypt
            EncryptSymmetricRequest er = new EncryptSymmetricRequest();
            er.contentToEncrypt = plaintext.getBytes();
            er.passphrase = passphrase;
            Envelope ee = Envelope.documentFactory();
            DLC.addData(EncryptSymmetricRequest.class, er, ee);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_ENCRYPT_SYMMETRIC, ee);
            ee.setRoute(ee.getDynamicRoutingSlip().nextRoute());
            service.handleDocument(ee);
            if(er.errorCode != ServiceRequest.NO_ERROR) System.out.println("Encryption Error Code: "+er.errorCode);
            assert er.encryptedContent != null;
            System.out.println("Encrypted text: \n"+er.encryptedContent);

            // Decrypt
            DecryptSymmetricRequest dr = new DecryptSymmetricRequest();
            dr.encryptedContent = er.encryptedContent;
            dr.iv = er.iv;
            dr.passphrase = passphrase;
            Envelope de = Envelope.documentFactory();
            DLC.addData(DecryptSymmetricRequest.class, dr, de);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_DECRYPT_SYMMETRIC, de);
            de.setRoute(de.getDynamicRoutingSlip().nextRoute());
            service.handleDocument(de);
            if(dr.errorCode != ServiceRequest.NO_ERROR) System.out.println("Decryption Error Code: "+dr.errorCode);
            assert dr.errorCode == ServiceRequest.NO_ERROR;
            assert plaintext.equals(new String(dr.decryptedContent));
            System.out.println("Decrypted text: \n"+new String(dr.decryptedContent));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
