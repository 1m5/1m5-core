package io.onemfive.core.keyring;

import io.onemfive.core.ServiceRequest;
import io.onemfive.data.Envelope;
import io.onemfive.data.content.Text;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class KeyRingServiceTest {

    KeyRingService service;

    String passphrase = "1234";
    String aliasOne = "Bob";
    String aliasTwo = "Charlie";

    @Before
    public void init() {

        service = new KeyRingService(null, null);
        service.start(null);

        // Generate Key Ring Collections
        if(!FileUtil.fileExists(aliasOne +".pkr")) {
            FileUtil.rmFile(aliasOne +".skr");
            GenerateKeyRingCollectionsRequest lr = new GenerateKeyRingCollectionsRequest();
            lr.keyRingUsername = aliasOne;
            lr.keyRingPassphrase = passphrase;
            Envelope e = Envelope.documentFactory();
            DLC.addData(GenerateKeyRingCollectionsRequest.class, lr, e);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_GENERATE_KEY_RINGS_COLLECTIONS, e);
            e.setRoute(e.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
            service.handleDocument(e);
        }
        if(!FileUtil.fileExists(aliasTwo +".pkr")) {
            FileUtil.rmFile(aliasTwo + ".skr");
            GenerateKeyRingCollectionsRequest lr2 = new GenerateKeyRingCollectionsRequest();
            lr2.keyRingUsername = aliasTwo;
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
        FileUtil.rmFile(aliasOne);
        FileUtil.rmFile(aliasTwo);
    }

    @Test
    public void genKeyRingCollection() {

        String aliasAlice = "Annie";
        String passphrase = "1234";

        // Generate Key Ring Collections
        GenerateKeyRingCollectionsRequest lr = new GenerateKeyRingCollectionsRequest();
        lr.keyRingUsername = aliasAlice;
        lr.keyRingPassphrase = passphrase;
        Envelope e = Envelope.documentFactory();
        DLC.addData(GenerateKeyRingCollectionsRequest.class, lr, e);
        DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_GENERATE_KEY_RINGS_COLLECTIONS, e);
        e.setRoute(e.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router

        long start = new Date().getTime();
        service.handleDocument(e);
        long end = new Date().getTime();
        long duration = end - start;
        System.out.println("Generate Key Ring Duration (ms): " + duration);
        assert FileUtil.fileExists("Annie.pkr");
        assert FileUtil.fileExists("Annie.skr");

        FileUtil.rmFile("Annie.pkr");
        FileUtil.rmFile("Annie.skr");
    }

    @Test
    public void authN() {

        AuthNRequest r = new AuthNRequest();
        r.keyRingUsername = aliasOne;
        r.keyRingPassphrase = passphrase;
        r.alias = aliasOne;
        r.aliasPassphrase = passphrase;
        r.autoGenerate = true;

        Envelope e = Envelope.documentFactory();
        DLC.addData(AuthNRequest.class, r, e);
        DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_AUTHN, e);
        e.setRoute(e.getDynamicRoutingSlip().nextRoute());
        long start = new Date().getTime();
        service.handleDocument(e);
        long end = new Date().getTime();
        long duration = end - start;
        System.out.println("Get Public Key authN Duration: " + duration);
        assert r.identityPublicKey != null && r.identityPublicKey.getAddress() != null;
        assert r.encryptionPublicKey != null & r.encryptionPublicKey.getAddress() != null;

        System.out.println("Identity Public Key Fingerprint: " + r.identityPublicKey.getFingerprint());
        System.out.println("Identity Public Key Address: " + r.identityPublicKey.getAddress());
        System.out.println("Identity Public Key Is Identity Key: " + r.identityPublicKey.isIdentityKey());
        System.out.println("Identity Public Key is Encryption Key: " + r.identityPublicKey.isEncryptionKey());

        System.out.println("Encryption Public Key Fingerprint: " + r.encryptionPublicKey.getFingerprint());
        System.out.println("Encryption Public Key Address: " + r.encryptionPublicKey.getAddress());
        System.out.println("Encryption Public Key Is Identity Key: " + r.encryptionPublicKey.isIdentityKey());
        System.out.println("Encryption Public Key is Encryption Key: " + r.encryptionPublicKey.isEncryptionKey());
    }

    @Test
    public void symmetricCryption() {
        String passphrase = "1234";
        String plaintext = "This is a secret";
        System.out.println("Content to encrypt: \n"+plaintext);
        try {
            // Encrypt
            EncryptSymmetricRequest er = new EncryptSymmetricRequest();
            er.content = new Text(plaintext.getBytes("UTF-8"));
            er.content.setEncryptionPassphrase(passphrase);
            Envelope ee = Envelope.documentFactory();
            DLC.addData(EncryptSymmetricRequest.class, er, ee);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_ENCRYPT_SYMMETRIC, ee);
            ee.setRoute(ee.getDynamicRoutingSlip().nextRoute());
            service.handleDocument(ee);
            if(er.errorCode != ServiceRequest.NO_ERROR) System.out.println("Encryption Error Code: "+er.errorCode);
            assert er.content.getBody() != null;
            System.out.println("Encrypted text: \n"+new String(er.content.getBody()));

            // Decrypt
            DecryptSymmetricRequest dr = new DecryptSymmetricRequest();
            dr.content = er.content;
            Envelope de = Envelope.documentFactory();
            DLC.addData(DecryptSymmetricRequest.class, dr, de);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_DECRYPT_SYMMETRIC, de);
            de.setRoute(de.getDynamicRoutingSlip().nextRoute());
            service.handleDocument(de);
            if(dr.errorCode != ServiceRequest.NO_ERROR) System.out.println("Decryption Error Code: "+dr.errorCode);
            assert dr.errorCode == ServiceRequest.NO_ERROR;
            assert plaintext.equals(new String(dr.content.getBody()));
            System.out.println("Decrypted text: \n"+new String(dr.content.getBody()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void asymmetricCryption() {
        String contentKey = "1234:iv-here:content-hash-here"; // secure random as passphrase:iv:content hash
        // Encrypt
        try {
            EncryptRequest er = new EncryptRequest();
            er.content = new Text(contentKey.getBytes("UTF-8"));
            er.keyRingUsername = aliasOne;
            er.keyRingPassphrase = passphrase;
            er.publicKeyAlias = aliasOne;
            Envelope ee = Envelope.documentFactory();
            DLC.addData(EncryptRequest.class, er, ee);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_ENCRYPT, ee);
            ee.setRoute(ee.getDynamicRoutingSlip().nextRoute());
            service.handleDocument(ee);
            assert er.errorCode == ServiceRequest.NO_ERROR;
            assert er.content.getEncrypted();

            // Decrypt
            DecryptRequest dr = new DecryptRequest();
            dr.content = er.content;
            dr.keyRingUsername = aliasOne;
            dr.keyRingPassphrase = passphrase;
            dr.alias = aliasOne;
            dr.content.setEncryptionPassphrase(passphrase);
            Envelope de = Envelope.documentFactory();
            DLC.addData(DecryptRequest.class, dr, de);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_DECRYPT, de);
            de.setRoute(de.getDynamicRoutingSlip().nextRoute());
            service.handleDocument(de);
            assert dr.errorCode == ServiceRequest.NO_ERROR;
            assert contentKey.equals(new String(dr.content.getBody()));
            System.out.println(new String(dr.content.getBody()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
