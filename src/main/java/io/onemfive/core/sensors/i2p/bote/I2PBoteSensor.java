package io.onemfive.core.sensors.i2p.bote;

import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.sensors.*;
import io.onemfive.core.sensors.i2p.I2PSensor;
import io.onemfive.core.sensors.i2p.bote.crypto.PublicKeyPair;
import io.onemfive.core.sensors.i2p.bote.email.*;
import io.onemfive.core.sensors.i2p.bote.email.Email;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.folder.EmailFolder;
import io.onemfive.core.sensors.i2p.bote.folder.NewEmailListener;
import io.onemfive.core.sensors.i2p.bote.network.NetworkStatus;
import io.onemfive.core.sensors.i2p.bote.network.NetworkStatusListener;
import io.onemfive.core.sensors.i2p.bote.status.ChangeIdentityStatus;
import io.onemfive.core.sensors.i2p.bote.status.StatusListener;
import io.onemfive.core.sensors.i2p.bote.util.BoteHelper;
import io.onemfive.core.sensors.i2p.bote.util.GeneralHelper;
import io.onemfive.core.util.Wait;
import io.onemfive.data.*;
import io.onemfive.data.util.DLC;
import net.i2p.data.DataHelper;
import net.i2p.router.Router;

import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.*;
import java.util.logging.Logger;

/**
 * Provides an API for I2P Bote as a Sensor.
 * I2P Bote in 1M5 is used as Message-Oriented-Middleware (MOM)
 * supporting delayed anonymous messaging for combating timing attacks.
 *
 * @author objectorange
 */
public class I2PBoteSensor extends I2PSensor implements NetworkStatusListener, NewEmailListener {

    private static final Logger LOG = Logger.getLogger(I2PBoteSensor.class.getName());

    public I2PBoteSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    protected SensorID getSensorID() {
        return SensorID.I2PBOTE;
    }

    /**
     * Sends an Envelope as an Email through I2P Bote.
     * @param e Envelope contains io.onemfive.data.Email in Data
     * @return boolean was successful
     */
    @Override
    public boolean send(Envelope e) {
        LOG.info("Sending I2P Bote Email...");
        io.onemfive.data.Email email = (io.onemfive.data.Email)DLC.getData(io.onemfive.data.Email.class,e);
        DID fromDID = email.getFromDID();
        DID toDID = email.getToDID();
        String subject = email.getSubject();
        String message = email.getMessage();

        InternetAddress sender;
        if(fromDID == null) {
            try {
                sender = new InternetAddress("Anonymous");
            } catch (AddressException e1) {
                e1.printStackTrace();
                LOG.warning("Unable to create Anonymous sender InternetAddress");
                return false;
            }
        } else if(fromDID.getEncodedKey() == null) {
            LOG.warning("From DID Encoded Key is null: alias="+fromDID.getAlias());
            return false;
        } else {
            try {
                sender = new InternetAddress(BoteHelper.getNameAndDestination(fromDID.getEncodedKey()));
            } catch (AddressException e1) {
                e1.printStackTrace();
                LOG.warning("Unable to build InternetAddress using FromDID with EncodedKey="+fromDID.getEncodedKey()+" and alias="+fromDID.getAlias());
                return false;
            } catch (PasswordException e1) {
                e1.printStackTrace();
                LOG.warning("Password either not present or incorrect when building InternetAddress using FromDID with EncodedKey="+fromDID.getEncodedKey()+" and alias="+fromDID.getAlias());
                return false;
            } catch (IOException e1) {
                e1.printStackTrace();
                LOG.warning("IOException caught when building InternetAddress using FromDID with EncodedKey="+fromDID.getEncodedKey()+" and alias="+fromDID.getAlias());
                return false;
            } catch (GeneralSecurityException e1) {
                e1.printStackTrace();
                LOG.warning("GeneralSecurityException caught when building InternetAddress using FromDID with EncodedKey="+fromDID.getEncodedKey()+" and alias="+fromDID.getAlias());
                return false;
            }
        }

        InternetAddress recipient = null;
        try {
            recipient = new InternetAddress(BoteHelper.getNameAndDestination(toDID.getEncodedKey()));
        } catch (AddressException e1) {
            e1.printStackTrace();
            LOG.warning("Unable to build InternetAddress using ToDID with EncodedKey="+toDID.getEncodedKey()+" and alias="+toDID.getAlias());
            return false;
        } catch (PasswordException e1) {
            e1.printStackTrace();
            LOG.warning("Password either not present or incorrect when building InternetAddress using ToDID with EncodedKey="+toDID.getEncodedKey()+" and alias="+toDID.getAlias());
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            LOG.warning("IOException caught when building InternetAddress using ToDID with EncodedKey="+toDID.getEncodedKey()+" and alias="+toDID.getAlias());
            return false;
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
            LOG.warning("GeneralSecurityException caught when building InternetAddress using ToDID with EncodedKey="+toDID.getEncodedKey()+" and alias="+toDID.getAlias());
            return false;
        }
        Email i2pEmail;
        if(subject != null && message != null) {
            i2pEmail = new Email(I2PBote.getInstance().getConfiguration().getIncludeSentTime());
            List<Attachment> attachments = new ArrayList<>();
            try {
                i2pEmail.setSender(sender);
                i2pEmail.addRecipient(Message.RecipientType.TO, recipient);
                i2pEmail.setSubject(subject);
                i2pEmail.setText(message);
                i2pEmail.setHeader("Content-Type",email.getMessageType());
            } catch (MessagingException e1) {
                e1.printStackTrace();
                LOG.warning("Issue setting sender, recipient, subject, or content.");
                return false;
            }
        } else {
            LOG.warning("subject and message must be provided.");
            return false;
        }

        // Cache the fact that we sent this email
        BoteHelper.setEmailSent(i2pEmail, true);

        // Send the email
        try {
            I2PBote.getInstance().sendEmail(i2pEmail);
            LOG.info("I2P Bote Email sent.");
        } catch (MessagingException e1) {
            e1.printStackTrace();
            LOG.warning("MessagingException caught sending I2P Email with messageID="+i2pEmail.getMessageID());
            return false;
        } catch (PasswordException e1) {
            e1.printStackTrace();
            LOG.warning("Password either not present or incorrect when sending I2P Email with messageID="+i2pEmail.getMessageID());
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            LOG.warning("IOException caught when sending I2P Email with messageID="+i2pEmail.getMessageID());
            return false;
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
            LOG.warning("GeneralSecurityException caught when sending I2P Email with messageID="+i2pEmail.getMessageID());
            return false;
        }
        return true;
    }

    public void getKeys(Envelope e) {
        DID did = e.getDID();
        LOG.info("Retrieving I2P Bote keys...");
        Identities identities = I2PBote.getInstance().getIdentities();
        EmailIdentity emailIdentity = null;
        try {
            emailIdentity = identities.getDefault();
            if(emailIdentity != null) {
                LOG.info("Default Identity found.");
            } else {
                LOG.info("Default Identity not found; must create.");
            }
        } catch (PasswordException e1) {
            LOG.warning(e1.getLocalizedMessage());
        } catch (IOException e1) {
            LOG.warning(e1.getLocalizedMessage());
        } catch (GeneralSecurityException e1) {
            LOG.warning(e1.getLocalizedMessage());
        }
        if(emailIdentity == null) {
            // No Default Identity therefore create one
            StatusListener<ChangeIdentityStatus> lsnr = new StatusListener<ChangeIdentityStatus>() {
                @Override
                public void updateStatus(ChangeIdentityStatus changeIdentityStatus, String... args) {
                    LOG.info(changeIdentityStatus.toString());
                }
            };
            /**
             * Args:
             * 1 = new?
             * 2 = Crypto IDs:
             *      1 = ElGamal-2048 / DSA-1024
             *      2 = ECDH-256 / ECDSA-256
             *      3 = ECDH-521 / ECDSA-521
             *      4 = NTRUEncrypt-1087 / GMSS-512
             * 3,4 = null
             * 5 = Alias/Public Name
             * 6-9 = null
             * 10 = default?
             * 11 = StatusListener
             */
            try {
                LOG.info("Creating default identities with alias: "+did.getAlias());
                GeneralHelper.createOrModifyIdentity(true, ElGamal2048DSA1024, null, null, did.getAlias(), null, null, null, null, true, lsnr);
                emailIdentity = identities.getDefault();
                LOG.info("Was new default identity created: "+Boolean.toString(emailIdentity != null));
                if(emailIdentity != null) {
                    LOG.info("Saving Identities...");
                    identities.save();
                }
            } catch (GeneralSecurityException e1) {
                LOG.warning(e1.getLocalizedMessage());
            } catch (PasswordException e1) {
                LOG.warning(e1.getLocalizedMessage());
            } catch (IOException e1) {
                LOG.warning(e1.getLocalizedMessage());
            } catch (IllegalDestinationParametersException e1) {
                LOG.warning(e1.getLocalizedMessage());
            }
        }
        if(emailIdentity != null) {
            LOG.info("Building up DID for alias "+did.getAlias()+"...");
            PublicKey publicEncryptionKey = emailIdentity.getPublicEncryptionKey();
//            PrivateKey privateEncryptionKey = emailIdentity.getPrivateEncryptionKey();
//            KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);
//            did.addEncryptionKeys(DID.Provider.I2P, did.getAlias(), encryptionKeyPair);

            PublicKey publicSigningKey = emailIdentity.getPublicSigningKey();
//            PrivateKey privateSigningKey = emailIdentity.getPrivateSigningKey();
//            KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
//            did.addIdentity(DID.Provider.I2P, did.getAlias(), signingKeyPair);

            PublicKeyPair publicKeyPair = new PublicKeyPair(publicEncryptionKey, publicSigningKey);
            try {
                did.addEncodedKey(emailIdentity.getCryptoImpl().toBase64(publicKeyPair));
                LOG.info("Base64 public key pair: "+did.getEncodedKey());
            } catch (GeneralSecurityException e1) {
                LOG.warning("GeneralSecurityException caught while converting public keys to base 64.");
            }
        }
    }

    @Override
    public void networkStatusChanged() {
        String statusText;
        switch (getNetworkStatus()) {
            case DELAY:
                statusText = "Waiting for I2P Network...";
                updateStatus(SensorStatus.NETWORK_WARMUP);
                break;
            case CONNECTING:
                statusText = "Connecting to I2P Network...";
                updateStatus(SensorStatus.NETWORK_CONNECTING);
                break;
            case CONNECTED:
                statusText = "Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case ERROR:
                statusText = "Error connecting to I2P Network.";
                updateStatus(SensorStatus.NETWORK_ERROR);
                break;
            case NOT_STARTED:
            default: {
                statusText = "Not connected to I2P Network.";
                updateStatus(SensorStatus.NETWORK_STOPPED);
            }
        }
        LOG.info(statusText);
    }

    public NetworkStatus getNetworkStatus() {
        return I2PBote.getInstance().getNetworkStatus();
    }

    @Override
    public void emailReceived(String messageId) {
        LOG.info("Received I2P Bote Email with messageId="+messageId);
        EmailFolder inbox = I2PBote.getInstance().getInbox();
        try {
            // Set the new email as \Recent
            inbox.setRecent(messageId, true);

            // Now send notifications for all \Recent emails
            List<Email> newEmails = BoteHelper.getRecentEmails(inbox);
            io.onemfive.data.Email email;
            for (Email i2pEmail : newEmails) {
                email = new io.onemfive.data.Email();

                DID fromDID = new DID();
                String fromAddress = (i2pEmail.getAllFromAddresses().toArray()[0]).toString();
                fromAddress = fromAddress.substring(fromAddress.indexOf("<")+1);
                fromAddress = fromAddress.substring(0,fromAddress.length()-1);
                fromDID.addEncodedKey(DID.Provider.I2P, fromAddress);
                email.setFromDID(fromDID);
                LOG.info("From Address: "+fromDID.getEncodedKey());

                DID toDID = new DID();
                String toAddress = i2pEmail.getToAddresses()[0].toString();
                toAddress = toAddress.substring(toAddress.indexOf("<")+1);
                toAddress = toAddress.substring(0,toAddress.length()-1);
                toDID.addEncodedKey(DID.Provider.I2P, toAddress);
                email.setToDID(toDID);
                LOG.info("To Address: "+toDID.getEncodedKey());

                email.setSubject(i2pEmail.getSubject());
                LOG.info("Email subject: "+email.getSubject());

                email.setMessage(i2pEmail.getText());
                LOG.info("Email text: "+email.getMessage());

                email.setMessageType(i2pEmail.getContentType());
                LOG.info("Email text type: "+email.getMessageType());

                // Indicate that it was received (for testing)
                email.setFlag(1);

                Envelope e = Envelope.eventFactory(EventMessage.Type.EMAIL);
                e.setDID(fromDID);
                EventMessage m = (EventMessage)e.getMessage();
                m.setMessage(email);
                m.setName(fromDID.getEncodedKey());
                DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH,e);
                sensorsService.sendToBus(e);

                // Email sent to Notification Service therefore delete from Inbox
                inbox.delete(i2pEmail.getMessageID());
            }
        } catch (PasswordException e) {
            LOG.warning(e.getLocalizedMessage());
        } catch (MessagingException e) {
            LOG.warning(e.getLocalizedMessage());
        } catch (GeneralSecurityException e) {
            LOG.warning(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean start(Properties p) {
        super.start(p);
        LOG.info("Starting I2P Bote Sensor...");
        // I2P Bote Sensor Starting
        updateStatus(SensorStatus.STARTING);

        // I2P Bote Directory
        String i2pBoteDir = i2pAppDir + "/i2pbote";
        File i2pBoteFolder = new File(i2pBoteDir);
        if(!i2pBoteFolder.exists())
            if(!i2pBoteFolder.mkdir()) {
                LOG.warning("Unable to create I2P Bote directory: " + i2pBoteDir);
                return false;
            }
        properties.setProperty("i2p.dir.bote",i2pBoteDir);
        // I2P Bote Logger Config File
        File i2pBoteLoggerConfigFile = new File(i2pBoteDir, "logger.config");
        Properties props = new Properties();
        props.put("logger.record.i2p.bote","DEBUG");
        props.put("logger.minimumOnScreenLevel","DEBUG");
        try {
            DataHelper.storeProps(props, i2pBoteLoggerConfigFile);
        } catch (IOException e) {
            LOG.warning("Unable to save logger.config to directory: "+i2pBoteDir);
        }

        // I2P Bote Configuration File
        File i2pBoteConfigFile = new File(i2pBoteDir,"i2pbote.config");
        boolean i2pBoteConfigFileIsNew = false;
        if(!i2pBoteConfigFile.exists()) {
            if(!i2pBoteConfigFile.mkdir()) {
                LOG.warning("Unable to create i2pbote.config in directory: "+i2pBoteDir);
                return false;
            }
            i2pBoteConfigFileIsNew = true;
        }
        props = new Properties();
        if(!i2pBoteConfigFileIsNew) {
            // Not a new config file; Load properties from previous saves
            try {
                DataHelper.loadProps(props, i2pBoteConfigFile);
            } catch (IOException e) {
                LOG.warning("Unable to load i2pbote.config in directory: " + i2pBoteDir);
                return false;
            }
        }
        // Now support overriding previous saves if any with new configurations from 1M5 sensors.config.
        for(String n : p.stringPropertyNames()){
            if(n.startsWith("1m5.sensors.i2p.bote.")) {
                props.put(n.substring("1m5.sensors.i2p.bote.".length()),p.getProperty(n));
            }
        }
        // Save the config file if properties are present
        if(props.size() > 0) {
            try {
                DataHelper.storeProps(props, i2pBoteConfigFile);
            } catch (IOException e) {
                LOG.warning("Unable to save i2pbote.config in directory: "+i2pBoteDir);
                return false;
            }
        }

        // Start I2P Bote
        LOG.info("Starting I2P Bote version "+I2PBote.getAppVersion()+"...");
        I2PBote.getInstance().startUp();
        I2PBote.getInstance().addNewEmailListener(this);
        I2PBote.getInstance().addNetworkStatusListener(this);
        LOG.info("I2P Bote Started");
        LOG.info("I2P Bote Sensor Started");
        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        super.restart();
        restartAttempts++;
        return gracefulShutdown() && start(this.properties);
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down...");
        updateStatus(SensorStatus.SHUTTING_DOWN);
        I2PBote.getInstance().shutDown();
        I2PBote.getInstance().removeNewEmailListener(this);
        I2PBote.getInstance().removeNetworkStatusListener(this);
        if(router != null) {
            router.shutdown(Router.EXIT_HARD);
        }
        updateStatus(SensorStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        super.gracefulShutdown();
        LOG.info("Gracefully shutting down...");
        updateStatus(SensorStatus.GRACEFULLY_SHUTDOWN);
        I2PBote.getInstance().shutDown();
        I2PBote.getInstance().removeNewEmailListener(this);
        I2PBote.getInstance().removeNetworkStatusListener(this);
        if(router != null) {
            router.shutdownGracefully(Router.EXIT_GRACEFUL);
        }
        updateStatus(SensorStatus.GRACEFULLY_SHUTDOWN);
        LOG.info("Gracefully shutdown.");
        return true;
    }

    public static void main(String[] args) {
        Properties p = new Properties();
        p.setProperty("1m5.dir.base",args[0]);

        I2PBoteSensor i2PBoteSensor = new I2PBoteSensor(null);
        i2PBoteSensor.start(p);

        long maxWaitMs = 10 * 60 * 1000; // 10 minutes
        long periodicWaitMs = 30 * 1000; // 30 seconds
        long currentWaitMs = 0;
        NetworkStatus status = NetworkStatus.NOT_STARTED;
        while(currentWaitMs < maxWaitMs || status == NetworkStatus.CONNECTED) {
            status = i2PBoteSensor.getNetworkStatus();
            LOG.info("I2PBote Network Status: "+status.name());
            if(status == NetworkStatus.CONNECTED) {
                Envelope e = Envelope.documentFactory();
                DLC.addContent("Hello World",e);
                i2PBoteSensor.send(e);
            }
            Wait.aMs(periodicWaitMs);
            currentWaitMs += periodicWaitMs;
        }
        i2PBoteSensor.gracefulShutdown();
    }
}
