package io.onemfive.core.keyring;

import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class KeyRingServiceTest {


    @Before
    public void init() {

    }

    @After
    public void teardown() {

    }

//    @Test
//    public void genKeyRingCollection() {
//
//        // Ensure cleanup
//        FileUtil.rmFile("Bob.pkr");
//        FileUtil.rmFile("Bob.skr");
//
//        KeyRingService service = new KeyRingService(null, null);
//        service.start(null);
//
//        String aliasAlice = "Bob";
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
//        assert FileUtil.fileExists("Bob.pkr");
//        assert FileUtil.fileExists("Bob.skr");
//    }

    @Test
    public void authN() {
        // Ensure cleanup
        FileUtil.rmFile("Bob.pkr");
        FileUtil.rmFile("Bob.skr");

        KeyRingService service = new KeyRingService(null, null);
        service.start(null);

        AuthNRequest r = new AuthNRequest();
        r.keyRingUsername = "Bob";
        r.keyRingPassphrase = "1234";
        r.alias = "Bob";
        r.aliasPassphrase = "1234";
        r.autoGenerate = true;
        Envelope e = Envelope.documentFactory();
        DLC.addData(AuthNRequest.class, r, e);
        DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_AUTHN, e);
        e.setRoute(e.getDynamicRoutingSlip().nextRoute());
        long start = new Date().getTime();
        service.handleDocument(e);
        long end = new Date().getTime();
        long duration = end - start;
        System.out.println("Get Public Key Duration: "+duration);
        assert r.publicKey != null && r.publicKey.getAddress()!=null;
        System.out.println("Public Key Fingerprint: "+r.publicKey.getFingerprint());
        System.out.println("Public Key Address: "+r.publicKey.getAddress());
        System.out.println("Public Key Is Identity Key: "+r.publicKey.isIdentityKey());
        System.out.println("Public Key is Encryption Key: "+r.publicKey.isEncryptionKey());
//        try {
//            // Alice
//            PGPPublicKey kAliceM = sAlice.keyRing.getPublicKey("Alice", true);
//            assert(kAliceM != null && kAliceM.isMasterKey());
//
//            PGPPublicKey kAliceE = sAlice.keyRing.getPublicKey("Alice", false);
//            assert(kAliceE != null && kAliceE.isEncryptionKey());
//
//            // Charlie
//            PGPPublicKey kCharlieM = sCharlie.keyRing.getPublicKey("Charlie", true);
//            assert(kCharlieM != null && kCharlieM.isMasterKey());
//
//            PGPPublicKey kCharlieE = sCharlie.keyRing.getPublicKey("Charlie", false);
//            assert(kCharlieE != null && kCharlieE.isEncryptionKey());
//
//            // Alice - Barbara
//            PGPPublicKey kBarbaraM = sAlice.keyRing.getPublicKey("Barbara", true);
//            assert(kBarbaraM != null && kBarbaraM.isMasterKey());
//
//            PGPPublicKey kBarbaraE = sAlice.keyRing.getPublicKey("Barbara", false);
//            assert(kBarbaraE != null && kBarbaraE.isEncryptionKey());
//
//            // Charlie - Dan
//            PGPPublicKey kDanM = sCharlie.keyRing.getPublicKey("Dan", true);
//            assert(kDanM != null && kDanM.isMasterKey());
//
//            PGPPublicKey kDanE = sCharlie.keyRing.getPublicKey("Dan", false);
//            assert(kDanE != null && kDanE.isEncryptionKey());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // Add each other's public keys
//        start = new Date().getTime();
//        try {
//            StorePublicKeysRequest r = new StorePublicKeysRequest();
//
//            PGPPublicKey cK = sCharlie.keyRing.getPublicKey("Charlie", false);
//            PGPPublicKey aK = sAlice.keyRing.getPublicKey("Alice", false);
//
//            List<PGPPublicKey> cPublicKeys = new ArrayList<>();
//            cPublicKeys.add(cK);
//            r.keyId = cK.getKeyID();
//            r.publicKeys = cPublicKeys;
//            sAlice.keyRing.storePublicKeys(r);
//
//            List<PGPPublicKey> aPublicKeys = new ArrayList<>();
//            aPublicKeys.add(aK);
//            r.keyId = aK.getKeyID();
//            r.publicKeys = aPublicKeys;
//            sCharlie.keyRing.storePublicKeys(r);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        end = new Date().getTime();
//        duration = end - start;
//        System.out.println("Add Public Key Duration: "+duration);
    }

//    @Test
//    public void cryption() {
//        KeyRingService service = new KeyRingService(null, null);
//        service.start(null);
//
//        String aliasAlice = "Alice";
//        String passphrase = "1234";
//        OpenPGPKeyRing kr = new OpenPGPKeyRing();
//        // Encrypt
//        try {
//            PGPPublicKey publicKey = kr.getPublicKey(kr.getPublicKeyRingCollection(aliasAlice, passphrase), aliasAlice, false);
//            if(publicKey.getFingerprint() != null) {
//                EncryptRequest er = new EncryptRequest();
//                er.keyRingUsername = aliasAlice;
//                er.keyRingPassphrase = passphrase;
//                er.fingerprint = publicKey.getFingerprint();
//                String contentStringToEncrypt = "Hello Charlie!";
//                er.contentToEncrypt = contentStringToEncrypt.getBytes();
//                Envelope ee = Envelope.documentFactory();
//                DLC.addData(EncryptRequest.class, er, ee);
//                DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_ENCRYPT, ee);
//                ee.setRoute(ee.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
//                long start = new Date().getTime();
//                service.handleDocument(ee);
//                long end = new Date().getTime();
//                long duration = end - start;
//                System.out.println("Encrypt Duration: "+duration);
//                System.out.println("Encrypted content: "+new String(er.encryptedContent));
//                assert (er.errorCode == EncryptRequest.NO_ERROR && er.encryptedContent != null && er.encryptedContent.length > 0);
//
//                DecryptRequest dr = new DecryptRequest();
//                dr.keyRingUsername = aliasAlice;
//                dr.keyRingPassphrase = passphrase;
//                dr.encryptedContent = er.encryptedContent;
//                dr.alias = aliasAlice;
//                dr.passphrase = passphrase;
//                Envelope de = Envelope.documentFactory();
//                DLC.addData(DecryptRequest.class, dr, de);
//                DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_DECRYPT, de);
//                de.setRoute(de.getDynamicRoutingSlip().nextRoute()); // ratchet ahead as we're not using internal router
//                start = new Date().getTime();
//                service.handleDocument(de);
//                duration = end - start;
//                System.out.println("Decrypt Duration: "+duration);
//                String contentStringDecrypted = new String(dr.plaintextContent);
//                System.out.println("Decrypted content: "+contentStringDecrypted);
//                assert (contentStringToEncrypt.equals(contentStringDecrypted));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
