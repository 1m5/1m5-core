package io.onemfive.core.sensors.i2p.bote.packet.dht;

import io.onemfive.core.sensors.i2p.bote.crypto.CryptoFactory;
import io.onemfive.core.sensors.i2p.bote.crypto.CryptoImplementation;
import io.onemfive.core.sensors.i2p.bote.email.EmailDestination;
import io.onemfive.core.sensors.i2p.bote.email.EmailIdentity;
import io.onemfive.core.sensors.i2p.bote.packet.TypeCode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

import net.i2p.crypto.SHA256Generator;
import net.i2p.data.Hash;
import net.i2p.util.Log;

/**
 * An <code>EncryptedEmailPacket</code> contains an encrypted <code>UnencryptedEmailPacket</code>
 * and additional data.
 * <p/>
 * The field <code>delVerificationHash</code> is the SHA-256 hash of a 32-byte block of data (the
 * delete authorization) that is part of <code>encryptedData</code>. Storage nodes will verify
 * the hash before deleting an <code>EncryptedEmailPacket</code>.
 * <p/>
 * After the recipient has received and decrypted an <code>EncryptedEmailPacket</code>, it sends
 * the delete authorization to the storage nodes in an {@link EmailPacketDeleteRequest}.
 */
@TypeCode('E')
public class EncryptedEmailPacket extends DhtStorablePacket {
    public static int MAX_OVERHEAD = 641;   // The maximum number of bytes by which EncryptedEmailPacket can be bigger than the UnencryptedEmailPacket

    private Log log = new Log(EncryptedEmailPacket.class);
    private Hash dhtKey;
    private long storeTime;   // in milliseconds since 1970
    private Hash delVerificationHash;
    private CryptoImplementation cryptoImpl;
    private byte[] encryptedData;   // an UnencryptedEmailPacket, converted to a byte array and encrypted

    /**
     * Creates an <code>EncryptedEmailPacket</code> from an <code>UnencryptedEmailPacket</code>.
     * The public key of <code>emailDestination</code> is used for encryption.
     * The store time is set to <code>0</code>.
     * @param unencryptedPacket
     * @param emailDestination
     * @throws GeneralSecurityException If an error occurred during encryption
     */
    public EncryptedEmailPacket(UnencryptedEmailPacket unencryptedPacket, EmailDestination emailDestination) throws GeneralSecurityException {
        storeTime = 0;
        byte[] delAuthorizationBytes = unencryptedPacket.getDeleteAuthorization().toByteArray();
        delVerificationHash = SHA256Generator.getInstance().calculateHash(delAuthorizationBytes);
        cryptoImpl = emailDestination.getCryptoImpl();

        encryptedData = cryptoImpl.encrypt(unencryptedPacket.toByteArray(), emailDestination.getPublicEncryptionKey());
        dhtKey = getDhtKey();
    }

    /**
     * Creates an <code>EncryptedEmailPacket</code> from raw datagram data.
     * To read the encrypted parts of the packet, {@link #decrypt(EmailIdentity)} must be called first.
     * @param data
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     */
    public EncryptedEmailPacket(byte[] data) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        super(data);
        ByteBuffer buffer = ByteBuffer.wrap(data, HEADER_LENGTH, data.length-HEADER_LENGTH);

        dhtKey = readHash(buffer);
        storeTime = buffer.getInt() * 1000L;
        delVerificationHash = readHash(buffer);
        byte cryptoImplId = buffer.get();
        cryptoImpl = CryptoFactory.getInstance(cryptoImplId);
        int encryptedLength = buffer.getShort() & 0xFFFF;   // length of the encrypted part of the packet
        encryptedData = new byte[encryptedLength];
        buffer.get(encryptedData);
    }

    /**
     * Returns a hash computed from </code>encryptedData</code>.
     */
    @Override
    public Hash getDhtKey() {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteArrayStream);

        try {
            dataStream.writeShort(encryptedData.length);
            dataStream.write(encryptedData);

            byte[] dataToHash = byteArrayStream.toByteArray();
            return SHA256Generator.getInstance().calculateHash(dataToHash);
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream/DataOutputStream.", e);
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the DHT key stored in the packet matches
     * the computed (from the encrypted data) DHT key.
     * @see #getDhtKey()
     */
    public boolean verifyPacketHash() {
        return getDhtKey().equals(dhtKey);
    }

    public Hash getDeleteVerificationHash() {
        return delVerificationHash;
    }

    /**
     * Returns the time the packet was stored in a file, in milliseconds since <code>1-1-1970</code>.
     */
    public long getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(long storeTime) {
        this.storeTime = storeTime;
    }

    public CryptoImplementation getCryptoImpl() {
        return cryptoImpl;
    }

    /**
     * Decrypts the encrypted part of the packet with the private key of an <code>EmailIdentity</code>.
     * The {@link CryptoImplementation} in the <code>EmailIdentity</code> must be the same as the one
     * in this <code>EncryptedEmailPacket</code>.
     * @param identity
     * @throws GeneralSecurityException
     * @throws InvalidCipherTextException
     */
    public UnencryptedEmailPacket decrypt(EmailIdentity identity) throws GeneralSecurityException {
        if (cryptoImpl != identity.getCryptoImpl())
            throw new IllegalArgumentException("CryptoImplementations don't match. Email Packet: <" + cryptoImpl.getName() + ">, Email Identity: <" + identity.getCryptoImpl().getName() + ">.");

        byte[] decryptedData = cryptoImpl.decrypt(encryptedData, identity.getPublicEncryptionKey(), identity.getPrivateEncryptionKey());
        return new UnencryptedEmailPacket(decryptedData);
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteArrayStream);

        try {
            writeHeader(dataStream);
            dataStream.write(dhtKey.toByteArray());
            dataStream.writeInt((int)(storeTime / 1000));   // store as seconds
            dataStream.write(delVerificationHash.toByteArray());
            dataStream.write(cryptoImpl.getId());
            dataStream.writeShort(encryptedData.length);
            dataStream.write(encryptedData);
        }
        catch (IOException e) {
            log.error("Can't write to ByteArrayOutputStream.", e);
        }
        return byteArrayStream.toByteArray();
    }

    @Override
    public String toString() {
        return super.toString() + ", DHTkey=" + dhtKey + ", tstamp=" + storeTime + ", alg=" + cryptoImpl.getName() + ", delVerifHash=" + delVerificationHash + ", encrLen=" + encryptedData.length;
    }
}
