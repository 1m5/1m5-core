package io.onemfive.core.sensors.i2p;

import io.onemfive.core.Config;
import io.onemfive.core.ServiceRequest;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.sensors.*;
import io.onemfive.core.util.Wait;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.data.EventMessage;
import io.onemfive.data.Peer;
import io.onemfive.data.util.DLC;
import net.i2p.I2PException;
import net.i2p.client.*;
import net.i2p.client.datagram.I2PDatagramDissector;
import net.i2p.client.datagram.I2PInvalidDatagramException;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Base64;
import net.i2p.data.DataFormatException;
import net.i2p.data.DataHelper;
import net.i2p.data.Destination;
import net.i2p.router.CommSystemFacade;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.util.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Provides an API for I2P Router as a Sensor.
 * I2P in 1M5 is used as Message-Oriented-Middleware (MOM)
 * supporting real-time anonymous messaging.
 *
 * @author objectorange
 */
public class I2PSensor extends BaseSensor implements I2PSessionMuxedListener {

    /**
     * 1 = ElGamal-2048 / DSA-1024
     * 2 = ECDH-256 / ECDSA-256
     * 3 = ECDH-521 / ECDSA-521
     * 4 = NTRUEncrypt-1087 / GMSS-512
     */
    protected static int ElGamal2048DSA1024 = 1;
    protected static int ECDH256ECDSA256 = 2;
    protected static int ECDH521EDCSA521 = 3;
    protected static int NTRUEncrypt1087GMSS512 = 4;

    private static final Logger LOG = Logger.getLogger(I2PSensor.class.getName());

    private static final String DEST_KEY_FILE_NAME = "local_dest.key";

    protected Properties properties;

    // I2P Router and Context
    private File i2pDir;
    private RouterContext routerContext;
    protected Router router;

    private String i2pBaseDir;
    protected String i2pAppDir;

    private I2PClient i2pClient;
    private I2PSession i2pSession;
    private I2PSocketManager socketManager;

    // I2CP parameters allowed in the config file
    // Undefined parameters use the I2CP defaults
    private static final String PARAMETER_I2CP_DOMAIN_SOCKET = "i2cp.domainSocket";
    private static final List<String> I2CP_PARAMETERS = Arrays.asList(new String[] {
            PARAMETER_I2CP_DOMAIN_SOCKET,
            "inbound.length",
            "inbound.lengthVariance",
            "inbound.quantity",
            "inbound.backupQuantity",
            "outbound.length",
            "outbound.lengthVariance",
            "outbound.quantity",
            "outbound.backupQuantity",
    });

    public I2PSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    protected SensorID getSensorID() {
        return SensorID.I2P;
    }

    @Override
    public Map<String, Peer> getPeers() {
        Map<String, Peer> peers = new HashMap<>();

        return peers;
    }

    /**
     * Sends UTF-8 content to a Destination using I2P.
     * @param envelope Envelope containing destination DID as entity and content in message data.
     *                 Destination DID must contain base64 encoded key for I2P.
     * @return boolean was successful
     */
    @Override
    public boolean send(Envelope envelope) {
        LOG.info("Sending I2P Message...");
        SensorRequest request = (SensorRequest)DLC.getData(SensorRequest.class,envelope);
        Peer toPeer = request.to.getPeer(Peer.NETWORK_I2P);
        if(toPeer == null) {
            LOG.warning("No Peer for I2P found in toDID while sending to I2P.");
            request.errorCode = SensorRequest.TO_PEER_REQUIRED;
            return false;
        }
        if(!Peer.NETWORK_I2P.equals(toPeer.getNetwork())) {
            LOG.warning("I2P requires an I2P Peer.");
            request.errorCode = SensorRequest.TO_PEER_WRONG_NETWORK;
            return false;
        }
        String content = (String) DLC.getContent(envelope);
        if(content == null) {
            LOG.warning("No content found in Envelope while sending to I2P.");
            request.errorCode = SensorRequest.NO_CONTENT;
            return false;
        }

        try {
            Destination destination = i2pSession.lookupDest(toPeer.getAddress());
            if(destination == null) {
                LOG.warning("I2P Peer Destination not found.");
                request.errorCode = SensorRequest.TO_PEER_NOT_FOUND;
                return false;
            }
            i2pSession.sendMessage(destination, content.getBytes());
            LOG.info("I2P Message sent.");
        } catch (I2PSessionException e) {
            String errMsg = "Exception while sending I2P message: " + e.getLocalizedMessage();
            LOG.warning(errMsg);
            request.exception = e;
            request.errorMessage = errMsg;
            return false;
        }
        return true;
    }

    @Override
    public boolean reply(Envelope envelope) {
        return false;
    }

    @Override
    public void messageAvailable(I2PSession session, int msgId, long size) {
        LOG.info("Message received by I2P Sensor...");
        byte[] msg = new byte[0];
        try {
            msg = session.receiveMessage(msgId);
        } catch (I2PSessionException e) {
            LOG.warning("Can't get new message from I2PSession: " + e.getLocalizedMessage());
        }
        if (msg == null) {
            LOG.warning("I2PSession returned a null message: msgId=" + msgId + ", size=" + size + ", " + session);
            return;
        }

        I2PDatagramDissector d = new I2PDatagramDissector();
        try {
            d.loadI2PDatagram(msg);
            byte[] payload = d.getPayload();
            Destination sender = d.getSender();
            Envelope e = Envelope.eventFactory(EventMessage.Type.TEXT);
            Peer from = new Peer(Peer.NETWORK_I2P, sender.toBase64());
            DID did = new DID();
            did.addPeer(from);
            e.setDID(did);
            EventMessage m = (EventMessage)e.getMessage();
            m.setMessage(new String(payload));
            m.setName(from.getAddress());
            DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
            sensorsService.sendToBus(e);
        }
        catch (DataFormatException e) {
            LOG.warning("Invalid datagram received: "+e.getLocalizedMessage());
        }
        catch (I2PInvalidDatagramException e) {
            LOG.warning("Datagram failed verification: "+e.getLocalizedMessage());
        }
        catch (Exception e) {
            LOG.severe("Error processing datagram: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void messageAvailable(I2PSession session, int msgId, long size, int proto, int fromPort, int toPort) {
        if (proto == I2PSession.PROTO_DATAGRAM)
            messageAvailable(session, msgId, size);
        else
            LOG.warning("Received unhandled message with proto="+proto+" and id="+msgId);
    }

    @Override
    public void reportAbuse(I2PSession i2PSession, int severity) {
        LOG.warning("I2P Session reporting abuse. Severity="+severity);
    }

    @Override
    public void disconnected(I2PSession session) {
        LOG.warning("I2P Session disconnected.");
    }

    @Override
    public void errorOccurred(I2PSession session, String message, Throwable throwable) {
        LOG.severe("Router says: "+message+": "+throwable.getLocalizedMessage());
    }

    /**
     * Sets up a {@link I2PSession}, using the I2P destination stored on disk or creating a new I2P
     * destination if no key file exists.
     */
    private void initializeSession() throws I2PSessionException {
        updateStatus(SensorStatus.INITIALIZING);
        Properties sessionProperties = new Properties();
        // set tunnel names
        sessionProperties.setProperty("inbound.nickname", "I2PSensor");
        sessionProperties.setProperty("outbound.nickname", "I2PSensor");
        sessionProperties.putAll(getI2CPOptions());

        // read the local destination key from the key file if it exists
        File destinationKeyFile = getDestinationKeyFile();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(destinationKeyFile);
            char[] destKeyBuffer = new char[(int)destinationKeyFile.length()];
            fileReader.read(destKeyBuffer);
            byte[] localDestinationKey = Base64.decode(new String(destKeyBuffer));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(localDestinationKey);
            socketManager = I2PSocketManagerFactory.createDisconnectedManager(inputStream, null, 0, sessionProperties);
        }
        catch (IOException e) {
            LOG.info("Destination key file doesn't exist or isn't readable." + e);
        } catch (I2PSessionException e) {
            // Won't happen, inputStream != null
        } finally {
            if (fileReader != null)
                try {
                    fileReader.close();
                }
                catch (IOException e) {
                    LOG.warning("Error closing file: <" + destinationKeyFile.getAbsolutePath() + ">" + e);
                }
        }

        // if the local destination key can't be read or is invalid, create a new one
        if (socketManager == null) {
            LOG.info("Creating new local destination key");
            try {
                ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
                i2pClient.createDestination(arrayStream);
                byte[] localDestinationKey = arrayStream.toByteArray();

                ByteArrayInputStream inputStream = new ByteArrayInputStream(localDestinationKey);
                socketManager = I2PSocketManagerFactory.createDisconnectedManager(inputStream, null, 0, sessionProperties);

                destinationKeyFile = new SecureFile(destinationKeyFile.getAbsolutePath());
                if (destinationKeyFile.exists()) {
                    File oldKeyFile = new File(destinationKeyFile.getPath() + "_backup");
                    if (!destinationKeyFile.renameTo(oldKeyFile))
                        LOG.warning("Cannot rename destination key file <" + destinationKeyFile.getAbsolutePath() + "> to <" + oldKeyFile.getAbsolutePath() + ">");
                }
                else
                if (!destinationKeyFile.createNewFile())
                    LOG.warning("Cannot create destination key file: <" + destinationKeyFile.getAbsolutePath() + ">");

                BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new SecureFileOutputStream(destinationKeyFile)));
                try {
                    fileWriter.write(Base64.encode(localDestinationKey));
                }
                finally {
                    fileWriter.close();
                }
            } catch (I2PException e) {
                LOG.warning("Error creating local destination key: " + e.getLocalizedMessage());
            } catch (IOException e) {
                LOG.warning("Error writing local destination key to file: " + e.getLocalizedMessage());
            }
        }

        i2pSession = socketManager.getSession();
        // Throws I2PSessionException if the connection fails
        i2pSession.connect();

        Destination localDestination = i2pSession.getMyDestination();
        LOG.info("I2PSensor Local destination key (base64): " + localDestination.toBase64());
        LOG.info("I2PSensor Local destination hash (base64): " + localDestination.calculateHash().toBase64());

        i2pSession.addMuxedSessionListener(this, I2PSession.PROTO_DATAGRAM, I2PSession.PORT_ANY);

    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting I2P Sensor...");
        updateStatus(SensorStatus.STARTING);
        // I2P Sensor Starting
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
        // Config Directory
        String i2pConfigDir = i2pBaseDir + "/config";
        File i2pConfigFolder = new File(i2pConfigDir);
        if(!i2pConfigFolder.exists())
            if(!i2pConfigFolder.mkdir())
                LOG.warning("Unable to create I2P config directory: " +i2pConfigDir);
        if(i2pConfigFolder.exists()) {
            System.setProperty("i2p.dir.config",i2pConfigDir);
            properties.setProperty("i2p.dir.config",i2pConfigDir);
        }
        // Router Directory
        String i2pRouterDir = i2pBaseDir + "/router";
        File i2pRouterFolder = new File(i2pRouterDir);
        if(!i2pRouterFolder.exists())
            if(!i2pRouterFolder.mkdir())
                LOG.warning("Unable to create I2P router directory: "+i2pRouterDir);
        if(i2pRouterFolder.exists()) {
            System.setProperty("i2p.dir.router",i2pRouterDir);
            properties.setProperty("i2p.dir.router",i2pRouterDir);
        }
        // PID Directory
        String i2pPIDDir = i2pBaseDir + "/pid";
        File i2pPIDFolder = new File(i2pPIDDir);
        if(!i2pPIDFolder.exists())
            if(!i2pPIDFolder.mkdir())
                LOG.warning("Unable to create I2P PID directory: "+i2pPIDDir);
        if(i2pPIDFolder.exists()) {
            System.setProperty("i2p.dir.pid",i2pPIDDir);
            properties.setProperty("i2p.dir.pid",i2pPIDDir);
        }
        // Log Directory
        String i2pLogDir = i2pBaseDir + "/log";
        File i2pLogFolder = new File(i2pLogDir);
        if(!i2pLogFolder.exists())
            if(!i2pLogFolder.mkdir())
                LOG.warning("Unable to create I2P log directory: "+i2pLogDir);
        if(i2pLogFolder.exists()) {
            System.setProperty("i2p.dir.log",i2pLogDir);
            properties.setProperty("i2p.dir.log",i2pLogDir);
        }
        // App Directory
        i2pAppDir = i2pBaseDir + "/app";
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
//        File[] allSeedCertificates = seedCertificates.listFiles();
//        if ( allSeedCertificates != null) {
//            for (File f : allSeedCertificates) {
//                LOG.info("Deleting old seed certificate: " + f);
//                FileUtil.rmdir(f, false);
//            }
//        }

        File sslCertificates = new File(certDir, "ssl");
//        File[] allSSLCertificates = sslCertificates.listFiles();
//        if ( allSSLCertificates != null) {
//            for (File f : allSSLCertificates) {
//                LOG.info("Deleting old ssl certificate: " + f);
//                FileUtil.rmdir(f, false);
//            }
//        }

        if(!copyCertificatesToBaseDir(seedCertificates, sslCertificates))
            return false;

        // Start I2P Router
        LOG.info("Starting I2P Router...");
        new Thread(new RouterStarter()).start();

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(1);

        i2pClient = I2PClientFactory.createClient();
        try {
            updateStatus(SensorStatus.WAITING);
            startSignal.await(3, TimeUnit.MINUTES);
            updateStatus(SensorStatus.STARTING);
            initializeSession();
            doneSignal.countDown();
        } catch (InterruptedException e) {
            LOG.warning("Start interrupted, exiting");
            updateStatus(SensorStatus.ERROR);
            return false;
        } catch (Exception e) {
            LOG.severe("Unable to init I2PSensor: "+e.getLocalizedMessage());
            updateStatus(SensorStatus.ERROR);
            return false;
        }
        LOG.info("Started.");
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
        if(router != null)
            router.restart();
        return true;
    }

    @Override
    public boolean shutdown() {
        updateStatus(SensorStatus.SHUTTING_DOWN);
        new Thread(new RouterStopper()).start();
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        updateStatus(SensorStatus.GRACEFULLY_SHUTTING_DOWN);
        // will teardown in 11 minutes or less
        new Thread(new RouterGracefulStopper()).start();
        return true;
    }

    private class RouterStarter implements Runnable {
        public void run() {
            router = new Router(properties);
            router.setKillVMOnEnd(false);
            router.runRouter();
            routerContext = router.getContext();
        }
    }

    private class RouterStopper implements Runnable {
        public void run() {
            if(router != null) {
                router.shutdown(Router.EXIT_HARD);
            }
            updateStatus(SensorStatus.SHUTDOWN);
        }
    }

    private class RouterGracefulStopper implements Runnable {
        public void run() {
            if(router != null) {
                router.shutdownGracefully(Router.EXIT_GRACEFUL);
            }
            updateStatus(SensorStatus.GRACEFULLY_SHUTDOWN);
        }
    }

    public Properties getI2CPOptions() {
        Properties opts = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (I2CP_PARAMETERS.contains(entry.getKey()))
                opts.put(entry.getKey(), entry.getValue());
        }
        return opts;
    }

    public File getDestinationKeyFile() {
        return new File(i2pDir, DEST_KEY_FILE_NAME);
    }

    public void routerStatusChanged() {
        String statusText;
        switch (getRouterStatus()) {
            case UNKNOWN:
                statusText = "Testing I2P Network...";
                updateStatus(SensorStatus.NETWORK_CONNECTING);
                break;
            case IPV4_DISABLED_IPV6_UNKNOWN:
                statusText = "IPV4 Disabled but IPV6 Testing...";
                updateStatus(SensorStatus.NETWORK_CONNECTING);
                break;
            case IPV4_FIREWALLED_IPV6_UNKNOWN:
                statusText = "IPV4 Firewalled but IPV6 Testing...";
                updateStatus(SensorStatus.NETWORK_CONNECTING);
                break;
            case IPV4_SNAT_IPV6_UNKNOWN:
                statusText = "IPV4 SNAT but IPV6 Testing...";
                updateStatus(SensorStatus.NETWORK_CONNECTING);
                break;
            case IPV4_UNKNOWN_IPV6_FIREWALLED:
                statusText = "IPV6 Firewalled but IPV4 Testing...";
                updateStatus(SensorStatus.NETWORK_CONNECTING);
                break;
            case OK:
                statusText = "Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case IPV4_DISABLED_IPV6_OK:
                statusText = "IPV4 Disabled but IPV6 OK: Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case IPV4_FIREWALLED_IPV6_OK:
                statusText = "IPV4 Firewalled but IPV6 OK: Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case IPV4_SNAT_IPV6_OK:
                statusText = "IPV4 SNAT but IPV6 OK: Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case IPV4_UNKNOWN_IPV6_OK:
                statusText = "IPV4 Testing but IPV6 OK: Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case IPV4_OK_IPV6_FIREWALLED:
                statusText = "IPV6 Firewalled but IPV4 OK: Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case IPV4_OK_IPV6_UNKNOWN:
                statusText = "IPV6 Testing but IPV4 OK: Connected to I2P Network.";
                restartAttempts = 0; // Reset restart attempts
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            case DISCONNECTED:
                statusText = "Disconnected from I2P Network.";
                updateStatus(SensorStatus.NETWORK_STOPPED);
                break;
            case DIFFERENT:
                statusText = "Symmetric NAT: Error connecting to I2P Network.";
                updateStatus(SensorStatus.NETWORK_ERROR);
                break;
            case HOSED:
                statusText = "Unable to open UDP port for I2P.";
                updateStatus(SensorStatus.NETWORK_PORT_CONFLICT);
                break;
            case IPV4_DISABLED_IPV6_FIREWALLED:
                statusText = "IPV4 Disabled and IPV6 Firewalled. Unable to connect to I2P network.";
                updateStatus(SensorStatus.NETWORK_BLOCKED);
                break;
            case REJECT_UNSOLICITED:
                statusText = "Firewalled. Unable to connect to I2P network.";
                updateStatus(SensorStatus.NETWORK_BLOCKED);
                break;
            default: {
                statusText = "Not connected to I2P Network.";
                updateStatus(SensorStatus.NETWORK_STOPPED);
            }
        }
        LOG.info(statusText);
    }

    public CommSystemFacade.Status getRouterStatus() {
        return routerContext.commSystem().getStatus();
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
        final String path = "io/onemfive/core/sensors/i2p/bote";
        // Android apps are doing this within their startup as unable to extract these files from jars
        if(properties.getProperty(Config.PROP_OPERATING_SYSTEM) != null) {
            if(!properties.getProperty(Config.PROP_OPERATING_SYSTEM).equals(Config.OS.Android.name())) {
                // Other - extract as jar
                String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                final File jarFile = new File(jarPath);
                if (jarFile.isFile()) {
                    // called by a user of the 1M5 Core jar
                    try {
                        final JarFile jar = new JarFile(jarFile);
                        JarEntry entry;
                        File f = null;
                        final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                        while (entries.hasMoreElements()) {
                            entry = entries.nextElement();
                            final String name = entry.getName();
                            if (name.startsWith(path + "/certificates/reseed/")) { //filter according to the path
                                if (!name.endsWith("/")) {
                                    String fileName = name.substring(name.lastIndexOf("/") + 1);
                                    LOG.info("fileName to save: " + fileName);
                                    f = new File(reseedCertificates, fileName);
                                }
                            }
                            if (name.startsWith(path + "/certificates/ssl/")) {
                                if (!name.endsWith("/")) {
                                    String fileName = name.substring(name.lastIndexOf("/") + 1);
                                    LOG.info("fileName to save: " + fileName);
                                    f = new File(sslCertificates, fileName);
                                }
                            }
                            if (f != null) {
                                boolean fileReadyToSave = false;
                                if (!f.exists() && f.createNewFile()) fileReadyToSave = true;
                                else if (f.exists() && f.delete() && f.createNewFile()) fileReadyToSave = true;
                                if (fileReadyToSave) {
                                    FileOutputStream fos = new FileOutputStream(f);
                                    byte[] byteArray = new byte[1024];
                                    int i;
                                    InputStream is = getClass().getClassLoader().getResourceAsStream(name);
                                    //While the input stream has bytes
                                    while ((i = is.read(byteArray)) > 0) {
                                        //Write the bytes to the output stream
                                        fos.write(byteArray, 0, i);
                                    }
                                    //Close streams to prevent errors
                                    is.close();
                                    fos.close();
                                    f = null;
                                } else {
                                    LOG.warning("Unable to save file from 1M5 jar and is required: " + name);
                                    return false;
                                }
                            }
                        }
                        jar.close();
                    } catch (IOException e) {
                        LOG.warning(e.getLocalizedMessage());
                        return false;
                    }
                }
            }
        } else {
            // called while testing in an IDE
            URL boteFolderURL = I2PSensor.class.getResource(path);
            File boteResFolder = null;
            try {
                boteResFolder = new File(boteFolderURL.toURI());
            } catch (URISyntaxException e) {
                LOG.warning("Unable to access bote resource directory.");
                return false;
            }
            File[] boteResFolderFiles = boteResFolder.listFiles();
            File certResFolder = null;
            for (File f : boteResFolderFiles) {
                if ("certificates".equals(f.getName())) {
                    certResFolder = f;
                    break;
                }
            }
            if (certResFolder != null) {
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
        return true;
    }

    public static void main(String[] args) {
        Properties p = new Properties();
        p.setProperty("1m5.dir.base",args[0]);

        I2PSensor sensor = new I2PSensor(null);
        sensor.start(p);

        long maxWaitMs = 10 * 60 * 1000; // 10 minutes
        long periodicWaitMs = 30 * 1000; // 30 seconds
        long currentWaitMs = 0;
        while(currentWaitMs < maxWaitMs || sensor.getStatus() == SensorStatus.NETWORK_CONNECTED) {
            LOG.info("I2P Network Status: "+sensor.getStatus().name());
            if(sensor.getStatus() == SensorStatus.NETWORK_CONNECTED) {
                Envelope e = Envelope.documentFactory();
                DLC.addContent("Hello World",e);
                sensor.send(e);
            }
            Wait.aMs(periodicWaitMs);
            currentWaitMs += periodicWaitMs;
        }
        sensor.gracefulShutdown();
    }
}
