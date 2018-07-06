package io.onemfive.core.sensors.i2p.bote;

import io.onemfive.core.Config;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.sensors.*;
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
import net.i2p.client.I2PClient;
import net.i2p.data.DataHelper;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.util.FileUtil;
import net.i2p.util.OrderedProperties;

import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
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
public class I2PBoteSensor extends BaseSensor implements NetworkStatusListener, NewEmailListener {

    /**
     * 1 = ElGamal-2048 / DSA-1024
     * 2 = ECDH-256 / ECDSA-256
     * 3 = ECDH-521 / ECDSA-521
     * 4 = NTRUEncrypt-1087 / GMSS-512
     */
    public static int ElGamal2048DSA1024 = 1;
    public static int ECDH256ECDSA256 = 2;
    public static int ECDH521EDCSA521 = 3;
    public static int NTRUEncrypt1087GMSS512 = 4;

    private static final Logger LOG = Logger.getLogger(I2PBoteSensor.class.getName());

    private Properties properties;

    // I2P Router and Context
    private File i2pDir;
    private RouterContext routerContext;
    private Router router;

    private String i2pBaseDir;
    private String i2pBoteDir;

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
                i2pEmail.setSubject(subject,"UTF-8");
                i2pEmail.setContent(message, attachments);
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

    @Override
    public boolean reply(Envelope envelope) {
        return false;
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
            PrivateKey privateEncryptionKey = emailIdentity.getPrivateEncryptionKey();
            KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);
            did.addEncryptionKeys(DID.Provider.I2P, did.getAlias(), encryptionKeyPair);

            PublicKey publicSigningKey = emailIdentity.getPublicSigningKey();
            PrivateKey privateSigningKey = emailIdentity.getPrivateSigningKey();
            KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
            did.addIdentity(DID.Provider.I2P, did.getAlias(), signingKeyPair);

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
                fromDID.addEncodedKey(i2pEmail.getOneFromAddress());
                email.setFromDID(fromDID);
                LOG.info("From Address: "+fromDID.getEncodedKey());

                DID toDID = new DID();
                toDID.addEncodedKey(i2pEmail.getOneRecipient());
                email.setToDID(toDID);
                LOG.info("To Address: "+toDID.getEncodedKey());

                email.setSubject(i2pEmail.getSubject());
                LOG.info("Email subject: "+email.getSubject());

                email.setMessage(i2pEmail.getText());
                LOG.info("Email text: "+email.getMessage());

                // Indicate that it was received (for testing)
                email.setFlag(1);

                Envelope e = Envelope.eventFactory(EventMessage.Type.EMAIL);
                e.setDID(fromDID);
                ((EventMessage)e.getMessage()).setMessage(email);
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
        LOG.info("Starting I2P Bote Sensor...");
        // I2P Bote Sensor Starting
        updateStatus(SensorStatus.STARTING);
        LOG.info("Loading I2P properties...");
        properties = p;
        // Set up I2P Directories within 1M5 base directory - Base MUST get created or exit
        i2pBaseDir = properties.getProperty("1m5.dir.base") + "/i2p";
        i2pDir = new File(i2pBaseDir);
        if(!i2pDir.exists())
            if(!i2pDir.mkdir()) {
                LOG.severe("Unable to create I2P base directory: "+i2pBaseDir+"; exiting...");
                return false;
            }
        System.setProperty("i2p.dir.base",i2pBaseDir);
        properties.setProperty("i2p.dir.base",i2pBaseDir);
        // I2P Config Directory
        String i2pConfigDir = i2pBaseDir + "/config";
        File i2pConfigFolder = new File(i2pConfigDir);
        if(!i2pConfigFolder.exists())
            if(!i2pConfigFolder.mkdir())
                LOG.warning("Unable to create I2P config directory: " +i2pConfigDir);
        if(i2pConfigFolder.exists()) {
            System.setProperty("i2p.dir.config",i2pConfigDir);
            properties.setProperty("i2p.dir.config",i2pConfigDir);
        }
        // I2P Router Directory
        String i2pRouterDir = i2pBaseDir + "/router";
        File i2pRouterFolder = new File(i2pRouterDir);
        if(!i2pRouterFolder.exists())
            if(!i2pRouterFolder.mkdir())
                LOG.warning("Unable to create I2P router directory: "+i2pRouterDir);
        if(i2pRouterFolder.exists()) {
            System.setProperty("i2p.dir.router",i2pRouterDir);
            properties.setProperty("i2p.dir.router",i2pRouterDir);
        }
        // I2P PID Directory
        String i2pPIDDir = i2pBaseDir + "/pid";
        File i2pPIDFolder = new File(i2pPIDDir);
        if(!i2pPIDFolder.exists())
            if(!i2pPIDFolder.mkdir())
                LOG.warning("Unable to create I2P PID directory: "+i2pPIDDir);
        if(i2pPIDFolder.exists()) {
            System.setProperty("i2p.dir.pid",i2pPIDDir);
            properties.setProperty("i2p.dir.pid",i2pPIDDir);
        }
        // I2P Log Directory
        String i2pLogDir = i2pBaseDir + "/log";
        File i2pLogFolder = new File(i2pLogDir);
        if(!i2pLogFolder.exists())
            if(!i2pLogFolder.mkdir())
                LOG.warning("Unable to create I2P log directory: "+i2pLogDir);
        if(i2pLogFolder.exists()) {
            System.setProperty("i2p.dir.log",i2pLogDir);
            properties.setProperty("i2p.dir.log",i2pLogDir);
        }
        // I2P App Directory
        String i2pAppDir = i2pBaseDir + "/app";
        File i2pAppFolder = new File(i2pAppDir);
        if(!i2pAppFolder.exists())
            if(!i2pAppFolder.mkdir())
                LOG.warning("Unable to create I2P app directory: "+i2pAppDir);
        if(i2pAppFolder.exists()) {
            System.setProperty("i2p.dir.app", i2pAppDir);
            properties.setProperty("i2p.dir.app", i2pAppDir);
        }

        // Running Internal I2P Router
        System.setProperty(I2PClient.PROP_TCP_HOST, "internal");
        System.setProperty(I2PClient.PROP_TCP_PORT, "internal");

        // Merge router.config files
        mergeRouterConfig(null);

        // Certificates
        File certDir = new File(i2pBaseDir, "certificates");
        if(!certDir.exists())
            if(!certDir.mkdir()) {
                LOG.severe("Unable to create certificates directory in: "+i2pBaseDir+"; exiting...");
                return false;
            }
        File seedDir = new File(certDir, "reseed");
        if(!seedDir.exists())
            if(!seedDir.mkdir()) {
                LOG.severe("Unable to create "+i2pBaseDir+"/certificates/reseed directory; exiting...");
                return false;
            }
        File sslDir = new File(certDir, "ssl");
        if(!sslDir.exists())
            if(!sslDir.mkdir()) {
                LOG.severe("Unable to create "+i2pBaseDir+"/certificates/ssl directory; exiting...");
                return false;
            }

        File seedCertificates = new File(certDir, "reseed");
        File[] allSeedCertificates = seedCertificates.listFiles();
        if ( allSeedCertificates != null) {
            for (File f : allSeedCertificates) {
                LOG.info("Deleting old seed certificate: " + f);
                FileUtil.rmdir(f, false);
            }
        }

        File sslCertificates = new File(certDir, "ssl");
        File[] allSSLCertificates = sslCertificates.listFiles();
        if ( allSSLCertificates != null) {
            for (File f : allSSLCertificates) {
                LOG.info("Deleting old ssl certificate: " + f);
                FileUtil.rmdir(f, false);
            }
        }

        if(!copyCertificatesToBaseDir(seedCertificates, sslCertificates))
            return false;

        // Start I2P Router
        LOG.info("Starting I2P Router...");
        router = new Router(properties);
        router.setKillVMOnEnd(false);
        router.runRouter();
        routerContext = router.getContext();

        // I2P Bote Directory
        i2pBoteDir = i2pAppDir + "/i2pbote";
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
        restartAttempts++;
        return gracefulShutdown() && start(this.properties);
    }

    @Override
    public boolean shutdown() {
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

    /**
     *  Load defaults from internal router.config on classpath,
     *  then add props from i2pDir/router.config overriding any from internal router.config,
     *  then override these with the supplied overrides if not null which would likely come from 3rd party app (not yet supported),
     *  then write back to i2pDir/router.config.
     *
     *  @param overrides local overrides or null
     */
    public void mergeRouterConfig(Properties overrides) {
        Properties props = new OrderedProperties();
        File f = new File(i2pBaseDir,"router.config");
        boolean i2pBaseRouterConfigIsNew = false;
        if(!f.exists()) {
            if(!f.mkdir()) {
                LOG.warning("While merging router.config files, unable to create router.config in i2pBaseDirectory: "+i2pBaseDir);
            } else {
                i2pBaseRouterConfigIsNew = true;
            }
        }
        InputStream i2pBaseRouterConfig = null;
        try {
            props.putAll(Config.loadFromClasspath("router.config"));

            if(!i2pBaseRouterConfigIsNew) {
                i2pBaseRouterConfig = new FileInputStream(f);
                DataHelper.loadProps(props, i2pBaseRouterConfig);
            }

            // override with user settings
            if (overrides != null)
                props.putAll(overrides);

            DataHelper.storeProps(props, f);
        } catch (Exception e) {
            LOG.warning("Exception caught while merging router.config properties: "+e.getLocalizedMessage());
        } finally {
            if (i2pBaseRouterConfig != null) try {
                i2pBaseRouterConfig.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     *  Copy all certificates found in resources/io/onemfive/core/sensors/i2p/bote/certificates
     *  into i2pBaseDir/certificates
     *
     *  @param reseedCertificates destination directory for reseed certificates
     *  @param sslCertificates destination directory for ssl certificates
     */
    private boolean copyCertificatesToBaseDir(File reseedCertificates, File sslCertificates) {
        URL boteFolderURL = I2PBoteSensor.class.getResource(".");
        File boteResFolder = null;
        try {
            boteResFolder = new File(boteFolderURL.toURI());
        } catch (URISyntaxException e) {
            LOG.warning("Unable to access bote resource directory.");
            return false;
        }
        File[] boteResFolderFiles = boteResFolder.listFiles();
        File certResFolder = null;
        for(File f : boteResFolderFiles) {
            if("certificates".equals(f.getName())) {
                certResFolder = f;
                break;
            }
        }
        if(certResFolder != null) {
            File[] folders = certResFolder.listFiles();
            for (File folder : folders) {
                if ("reseed".equals(folder.getName())) {
                    File[] reseedCerts = folder.listFiles();
                    for (File reseedCert : reseedCerts) {
                        FileUtil.copy(reseedCert, reseedCertificates, true, false);
                    }
                } else if ("ssl".equals(folder.getName())) {
                    File[] sslCerts = folder.listFiles();
                    for (File sslCert : sslCerts) {
                        FileUtil.copy(sslCert, sslCertificates, true, false);
                    }
                }
            }
            return true;
        }
        return false;
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
