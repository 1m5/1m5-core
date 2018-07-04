package io.onemfive.core.sensors.i2p;

import io.onemfive.core.sensors.*;
import io.onemfive.core.util.AppThread;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;
import net.i2p.I2PException;
import net.i2p.client.I2PClient;
import net.i2p.client.I2PClientFactory;
import net.i2p.client.I2PSession;
import net.i2p.client.I2PSessionException;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Base64;
import net.i2p.data.Destination;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.util.I2PAppThread;
import net.i2p.util.SecureFile;
import net.i2p.util.SecureFileOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Provides an API for I2P Router as a Sensor.
 * I2P in 1M5 is used as Message-Oriented-Middleware (MOM)
 * supporting real-time anonymous messaging.
 *
 * @author objectorange
 */
public class I2PSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(I2PSensor.class.getName());

    private static final String DEST_KEY_FILE_NAME = "local_dest.key";

    // I2P Router
    private RouterContext routerContext;
    private Router router;
    private Properties properties;
    private AppThread starterThread;
    private I2PClient i2pClient;
    private I2PSession i2pSession;
    private I2PSocketManager socketManager;
    private File i2pDir;

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
    public boolean send(Envelope envelope) {
        String content = (String)DLC.getContent(envelope);

        return true;
    }

    @Override
    public boolean reply(Envelope envelope) {
        return false;
    }

    /**
     * Sets up a {@link I2PSession}, using the I2P destination stored on disk or creating a new I2P
     * destination if no key file exists.
     */
    private void initializeSession() throws I2PSessionException {
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
        LOG.info("Local destination key (base64): " + localDestination.toBase64());
        LOG.info("Local destination hash (base64): " + localDestination.calculateHash().toBase64());
    }

    /**
     * Initializes daemon threads, doesn't start them yet.
     */
    private void initializeServices() {

    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Loading properties...");
        properties = p;
        // Set up I2P Directories within 1M5 base directory - Base MUST get created or exit
        String i2pBaseDir = properties.getProperty("1m5.dir.base") + "/i2p";
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
        String i2pAppDir = i2pBaseDir + "/app";
        File i2pAppFolder = new File(i2pAppDir);
        if(!i2pAppFolder.exists())
            if(!i2pAppFolder.mkdir())
                LOG.warning("Unable to create I2P app directory: "+i2pAppDir);
        if(i2pAppFolder.exists()) {
            System.setProperty("i2p.dir.app", i2pAppDir);
            properties.setProperty("i2p.dir.app", i2pAppDir);
        }

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
            LOG.severe("Unable to start I2PSensor: "+e.getLocalizedMessage());
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
        // will shutdown in 11 minutes or less
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
                updateStatus(SensorStatus.SHUTDOWN);
            }
        }
    }

    private class RouterGracefulStopper implements Runnable {
        public void run() {
            if(router != null) {
                router.shutdownGracefully(Router.EXIT_GRACEFUL);
                updateStatus(SensorStatus.GRACEFULLY_SHUTDOWN);
            }
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

    public static void main(String[] args) {
        Properties p = new Properties();
        p.setProperty("1m5.dir.base","/Users/Brian/Projects/1m5/core/.1m5");
        I2PSensor sensor = new I2PSensor(null);
        sensor.start(p);
        sensor.gracefulShutdown();
    }
}
