package io.onemfive.core;

import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.infovault.LocalFSInfoVaultDB;
import io.onemfive.core.util.data.Base64;
import io.onemfive.core.util.*;
import io.onemfive.core.util.stat.StatManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>Provide a scope for accessing services that 15M5 provides.  Rather than
 * using the traditional singleton, where any component can access the component
 * in question directly, all of those 1M5 related services are exposed through
 * a particular OneMFiveAppContext. This helps not only with understanding their use
 * and the services 1M5 provides, but it also allows multiple isolated
 * environments to operate concurrently within the same JVM - particularly useful
 * for stubbing out implementations of the rooted services and simulating the
 * software's interaction between multiple instances.</p>
 *
 * <p>As a simplification, there is also a global context - if some component needs
 * access to one of the services but doesn't have its own context from which
 * to root itself, it binds to the OneMFiveAppContext's globalAppContext(), which is
 * the first context that was created within the JVM, or a new one if no context
 * existed already.  This functionality is often used within the 1M5 core for
 * logging - e.g. <pre>
 *     private static final Log _log = new Log(someClass.class);
 * </pre>
 * It is for this reason that applications that care about working with multiple
 * contexts should build their own context as soon as possible (within the main(..))
 * so that any referenced components will latch on to that context instead of
 * instantiating a new one.  However, there are situations in which both can be
 * relevant.</p>
 *
 * @author I2P, objectorange
 */
public class OneMFiveAppContext {

    private static final Logger LOG = Logger.getLogger(OneMFiveAppContext.class.getName());

    /** the context that components without explicit root are bound */
    protected static OneMFiveAppContext globalAppContext;
//    protected final OneMFiveConfig config;

    protected final Properties overrideProps = new Properties();
    private Properties envProps;

    private StatManager statManager;
    private LogManager logManager;
    private SimpleTimer simpleTimer;

    private ServiceBus serviceBus;

    private InfoVaultDB infoVaultDB;

    private volatile boolean statManagerInitialized;
    private volatile boolean logManagerInitialized;
    private volatile boolean simpleTimerInitialized;

    protected Set<Runnable> shutdownTasks;
    private File baseDir;
    private File configDir;
    private File pidDir;
    private File logDir;
    private File dataDir;
    private File cacheDir;
    private volatile File tmpDir;
    private File servicesDir;
    private final Random tmpDirRand = new Random();
    private static ClientAppManager clientAppManager;
    private final static Object lockA = new Object();
    private boolean initialize = false;
    private boolean configured = false;
    // split up big lock on this to avoid deadlocks
    private final Object lock1 = new Object(), lock2 = new Object(), lock3 = new Object(), lock4 = new Object();

    /**
     * Pull the default context, creating a new one if necessary, else using
     * the first one created.
     *
     * Warning - do not save the returned value, or the value of any methods below,
     * in a static field, or you will get the old context if a new instance is
     * started in the same JVM after the first is shut down,
     * e.g. on Android.
     */
    public static synchronized OneMFiveAppContext getInstance() {
        return getInstance(null);
    }

    public static OneMFiveAppContext getInstance(Properties properties) {
        synchronized (lockA) {
            if (globalAppContext == null) {
                globalAppContext = new OneMFiveAppContext(false, properties);
                LOG.info("Created and returning new instance: " + globalAppContext);
            } else {
                LOG.info("Returning cached instance: " + globalAppContext);
            }
        }
        if(!globalAppContext.configured) {
            globalAppContext.configure();
        }
        return globalAppContext;
    }

    public static void clearGlobalContext() {
        globalAppContext = null;
    }

    /**
     * Create a new context.
     *
     * @param doInit should this context be used as the global one (if necessary)?
     *               Will only apply if there is no global context now.
     */
    private OneMFiveAppContext(boolean doInit, Properties envProps) {
        this.initialize = doInit;
        this.envProps = envProps;
    }

    private void configure() {
        // set early to ensure it's not called twice
        this.configured = true;
        try {
            overrideProps.putAll(Config.loadFromClasspath("1m5.config", envProps, false));
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        shutdownTasks = new ConcurrentHashSet<>(10);

        String version = getProperty("1m5.version");
        LOG.info("1M5 Version: "+version);

        String systemTimeZone = getProperty("1m5.systemTimeZone");
        LOG.info("1M5 System Time Zone: "+systemTimeZone);
        TimeZone.setDefault(TimeZone.getTimeZone(systemTimeZone));

        String baseStr = getProperty("1m5.dir.base");
        if(baseStr!=null) {
            baseDir = new File(baseStr);
            if (!baseDir.exists() && !baseDir.mkdir()) {
                LOG.warning("Unable to create 1m5.dir.base: " + baseStr);
                return;
            }
        }  else {
            try {
                baseDir = SystemSettings.getUserAppHomeDir("1m5","core",true);
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
                return;
            }
            if(baseDir!=null) {
                overrideProps.put("1m5.dir.base", baseDir.getAbsolutePath());
            } else {
                baseDir = SystemSettings.getSystemApplicationDir("1m5", "core", true);
                if (baseDir == null) {
                    LOG.severe("Unable to create base system directory for 1M5 core.");
                    return;
                } else {
                    baseStr = baseDir.getAbsolutePath();
                    overrideProps.put("1m5.dir.base", baseStr);
                }
            }
        }
        LOG.info("1M5 Base Directory: "+baseStr);

        configDir = new SecureFile(baseDir, "config");
        if(!configDir.exists() && !configDir.mkdir()) {
            LOG.severe("Unable to create config directory in 1M5 base directory.");
            return;
        } else {
            overrideProps.put("1m5.dir.config",configDir.getAbsolutePath());
        }

        dataDir = new SecureFile(baseDir, "data");
        if(!dataDir.exists() && !dataDir.mkdir()) {
            LOG.severe("Unable to create data directory in 1M5 base directory.");
            return;
        } else {
            overrideProps.put("1m5.dir.data",dataDir.getAbsolutePath());
        }

        cacheDir = new SecureFile(baseDir, "cache");
        if(!cacheDir.exists() && !cacheDir.mkdir()) {
            LOG.severe("Unable to create cache directory in 1M5 base directory.");
            return;
        } else {
            overrideProps.put("1m5.dir.cache",cacheDir.getAbsolutePath());
        }

        pidDir = new SecureFile(baseDir, "pid");
        if (!pidDir.exists() && !pidDir.mkdir()) {
            LOG.severe("Unable to create pid directory in 1M5 base directory.");
            return;
        } else {
            overrideProps.put("1m5.dir.pid",pidDir.getAbsolutePath());
        }

        logDir = new SecureFile(baseDir, "logs");
        if (!logDir.exists() && !logDir.mkdir()) {
            LOG.severe("Unable to create logs directory in 1M5 base directory.");
            return;
        } else {
            overrideProps.put("1m5.dir.log",logDir.getAbsolutePath());
        }

        tmpDir = new SecureFile(baseDir, "tmp");
        if (!tmpDir.exists() && !tmpDir.mkdir()) {
            LOG.severe("Unable to create tmp directory in 1M5 base directory.");
            return;
        } else {
            overrideProps.put("1m5.dir.temp",tmpDir.getAbsolutePath());
        }

        servicesDir = new SecureFile(baseDir, "services");
        if (!servicesDir.exists() && !servicesDir.mkdir()) {
            LOG.severe("Unable to create services directory in 1M5 base directory.");
            return;
        } else {
            overrideProps.put("1m5.dir.services",servicesDir.getAbsolutePath());
        }

        LOG.info("1M5 Directories: " +
                "\n\tBase: "+baseDir.getAbsolutePath()+
                "\n\tConfig: "+configDir.getAbsolutePath()+
                "\n\tData: "+dataDir.getAbsolutePath()+
                "\n\tCache: "+cacheDir.getAbsolutePath()+
                "\n\tPID: "+pidDir.getAbsolutePath()+
                "\n\tLogs: "+logDir.getAbsolutePath()+
                "\n\tTemp: "+tmpDir.getAbsolutePath()+
                "\n\tServices: "+servicesDir.getAbsolutePath());

        clientAppManager = new ClientAppManager(false);
        // Instantiate Service Bus
        serviceBus = new ServiceBus(overrideProps, clientAppManager);

        if (initialize) {
            if (globalAppContext == null) {
                globalAppContext = this;
            } else {
                LOG.warning("Warning - New context not replacing old one, you now have an additional one");
                (new Exception("I did it")).printStackTrace();
            }
        }

        // InfoVaultDB
        overrideProps.setProperty("1m5.dir.services.io.onemfive.core.infovault.InfoVaultService", servicesDir.getAbsolutePath() + "/io.onemfive.core.infovault.InfoVaultService");
        try {
            if(overrideProps.getProperty(InfoVaultDB.class.getName()) != null) {
                LOG.info("Instantiating InfoVaultDB of type: "+overrideProps.getProperty(InfoVaultDB.class.getName()));
                infoVaultDB = InfoVaultService.getInfoVaultDBInstance(overrideProps.getProperty(InfoVaultDB.class.getName()));
            } else {
                LOG.info("No InfoVaultDB type provided. Instantiating InfoVaultDB of default type: "+LocalFSInfoVaultDB.class.getName());
                infoVaultDB = InfoVaultService.getInfoVaultDBInstance(LocalFSInfoVaultDB.class.getName());
            }
            infoVaultDB.init(overrideProps);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        this.configured = true;
    }

    public InfoVaultDB getInfoVaultDB() {
        return infoVaultDB;
    }

    public ClientAppManager getClientAppManager(Properties props) {
        if(clientAppManager.getStatus() == ClientAppManager.Status.STOPPED)
            clientAppManager.initialize(props);
        return clientAppManager;
    }

    public ServiceBus getServiceBus() {
        return serviceBus;
    }

    /**
     *  This is the installation dir, often referred to as $1m5.
     *  Applications should consider this directory read-only and never
     *  attempt to write to it.
     *  It may actually be read-only on a multi-user installation.
     *
     *  In Linux, the path is: /usr/share/1m5/core
     *  In Mac, the path is: /Applications/1m5/core
     *  in Windows, the path is: C:\\\\Program Files\\1m5\\core
     *
     *  @return File constant for the life of the context
     */
    public File getBaseDir() { return baseDir; }

    /**
     *  The direcory for core config files.
     *  Dapps may use this to read router configuration files if necessary.
     *  There may also be config files in this directory as templates for user
     *  installations that should not be altered by dapps.
     *
     *  1m5/core/config
     *
     *  @return File constant for the life of the context
     */
    public File getConfigDir() { return configDir; }

    /**
     *  The OS process id of the currently running instance.
     *  Dapps should not use this.
     *
     *  1m5/core/pid
     *
     *  @return File constant for the life of the context
     */
    public File getPIDDir() { return pidDir; }

    /**
     *  Where the log directory is.
     *  Dapps should not use this.
     *
     *  1m5/core/log
     *
     *  @return File constant for the life of the context
     */
    public File getLogDir() { return logDir; }

    /**
     *  Where the core stores core-specific data.
     *  Applications should create their own data directory within their base directory.
     *
     *  1m5/core/data
     *
     *  @return File constant for the life of the context
     */
    public File getDataDir() { return dataDir; }

    /**
     *  Where the core may store cache.
     *  Applications should create their own cache directory within their base directory.
     *
     *  1m5/core/cache
     *
     *  @return File constant for the life of the context
     */
    public File getCacheDir() { return cacheDir; }

    /**
     *  Where the core stores temporary data.
     *  This directory is created on the first call in this context and is deleted on JVM exit.
     *  Applications should create their own temp directory within their base directory.
     *
     *  1m5/core/tmp
     *
     *  @return File constant for the life of the context
     */
    public File getTempDir() {
        // fixme don't synchronize every time
        synchronized (lock1) {
            if (tmpDir == null) {
                String d = getProperty("1m5.dir.temp", System.getProperty("java.io.tmpdir"));
                // our random() probably isn't warmed up yet
                byte[] rand = new byte[6];
                tmpDirRand.nextBytes(rand);
                String f = "1m5-" + Base64.encode(rand) + ".tmp";
                tmpDir = new SecureFile(d, f);
                if (tmpDir.exists()) {
                    // good or bad ? loop and try again?
                } else if (tmpDir.mkdir()) {
                    tmpDir.deleteOnExit();
                } else {
                    LOG.warning("WARNING: Could not create temp dir " + tmpDir.getAbsolutePath());
                    tmpDir = new SecureFile(baseDir, "tmp");
                    tmpDir.mkdirs();
                    if (!tmpDir.exists())
                        LOG.severe("ERROR: Could not create temp dir " + tmpDir.getAbsolutePath());
                }
            }
        }
        return tmpDir;
    }

    /** don't rely on deleteOnExit() */
    public void deleteTempDir() {
        synchronized (lock1) {
            if (tmpDir != null) {
                FileUtil.rmdir(tmpDir, false);
                tmpDir = null;
            }
        }
    }

    /**
     * Access the configuration attributes of this context, using properties
     * provided during the context construction, or falling back on
     * System.getProperty if no properties were provided during construction
     * (or the specified prop wasn't included).
     *
     */
    public String getProperty(String propName) {
        String rv = overrideProps.getProperty(propName);
        if (rv != null)
            return rv;
        return System.getProperty(propName);
    }

    /**
     * Access the configuration attributes of this context, using properties
     * provided during the context construction, or falling back on
     * System.getProperty if no properties were provided during construction
     * (or the specified prop wasn't included).
     *
     */
    public String getProperty(String propName, String defaultValue) {
        if (overrideProps.containsKey(propName))
            return overrideProps.getProperty(propName, defaultValue);
        return System.getProperty(propName, defaultValue);
    }

    /**
     * Return an int with an int default
     */
    public int getProperty(String propName, int defaultVal) {
        String val = overrideProps.getProperty(propName);
        if (val == null)
            val = System.getProperty(propName);
        int ival = defaultVal;
        if (val != null) {
            try {
                ival = Integer.parseInt(val);
            } catch (NumberFormatException nfe) {LOG.warning(nfe.getLocalizedMessage());}
        }
        return ival;
    }

    /**
     * Return a long with a long default
     */
    public long getProperty(String propName, long defaultVal) {
        String val  = overrideProps.getProperty(propName);
        if (val == null)
            val = System.getProperty(propName);
        long rv = defaultVal;
        if (val != null) {
            try {
                rv = Long.parseLong(val);
            } catch (NumberFormatException nfe) {LOG.warning(nfe.getLocalizedMessage());}
        }
        return rv;
    }

    /**
     * Return a boolean with a boolean default
     */
    public boolean getProperty(String propName, boolean defaultVal) {
        String val = getProperty(propName);
        if (val == null)
            return defaultVal;
        return Boolean.parseBoolean(val);
    }

    /**
     * Default false
     */
    public boolean getBooleanProperty(String propName) {
        return Boolean.parseBoolean(getProperty(propName));
    }

    public boolean getBooleanPropertyDefaultTrue(String propName) {
        return getProperty(propName, true);
    }

    /**
     * Access the configuration attributes of this context, listing the properties
     * provided during the context construction, as well as the ones included in
     * System.getProperties.
     *
     * WARNING - not overridden in ConsciousContext, doesn't contain router config settings,
     * use getProperties() instead.
     *
     * @return set of Strings containing the names of defined system properties
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<String> getPropertyNames() {
        // clone to avoid ConcurrentModificationException
        Set<String> names = new HashSet<String>((Set<String>) (Set) ((java.util.Properties) System.getProperties().clone()).keySet()); // TODO-Java6: s/keySet()/stringPropertyNames()/
        if (overrideProps != null)
            names.addAll((Set<String>) (Set) overrideProps.keySet()); // TODO-Java6: s/keySet()/stringPropertyNames()/
        return names;
    }

    /**
     * Access the configuration attributes of this context, listing the properties
     * provided during the context construction, as well as the ones included in
     * System.getProperties.
     *
     * @return new Properties with system and context properties
     */
    public Properties getProperties() {
        // clone to avoid ConcurrentModificationException
        Properties props = new Properties();
        props.putAll((java.util.Properties)System.getProperties().clone());
        props.putAll(overrideProps);
        return props;
    }

    /**
     *  WARNING - Shutdown tasks are not executed in an I2PAppContext.
     *  You must be in a RouterContext for the tasks to be executed
     *  at teardown.
     *  This method moved from Router in 0.7.1 so that clients
     *  may use it without depending on router.jar.
     */
    public void addShutdownTask(Runnable task) {
        shutdownTasks.add(task);
    }

    /**
     *  @return an unmodifiable Set
     */
    public Set<Runnable> getShutdownTasks() {
        return Collections.unmodifiableSet(shutdownTasks);
    }

    /**
     *  Use this instead of context instanceof CoreContext
     */
    public boolean isConsciousContext() {
        return false;
    }

    /**
     * The statistics component with which we can track various events
     * over time.
     */
    public StatManager statManager() {
        if (!statManagerInitialized)
            initializeStatManager();
        return statManager;
    }

    private void initializeStatManager() {
        synchronized (lock2) {
            if (statManager == null)
                statManager = new StatManager(this);
            statManagerInitialized = true;
        }
    }

    /**
     * Query the log manager for this context, which may in turn have its own
     * set of configuration settings (loaded from the context's properties).
     * Each context's logManager keeps its own isolated set of Log instances with
     * their own log levels, output locations, and rotation configuration.
     */
    public LogManager logManager() {
        if (!logManagerInitialized)
            initializeLogManager();
        return logManager;
    }

    private void initializeLogManager() {
        synchronized (lock4) {
            if (logManager == null)
                logManager = new LogManager(this);
            logManagerInitialized = true;
        }
    }

    /**
     *  Is the wrapper present?
     */
    public boolean hasWrapper() {
        return System.getProperty("wrapper.version") != null;
    }

    /**
     * Use instead of SimpleTimer2.getInstance()
     * @since 0.9 to replace static instance in the class
     */
    public SimpleTimer simpleTimer() {
        if (!simpleTimerInitialized)
            initializeSimpleTimer();
        return simpleTimer;
    }

    private void initializeSimpleTimer() {
        synchronized (lock3) {
            if (simpleTimer == null)
                simpleTimer = new SimpleTimer(this);
            simpleTimerInitialized = true;
        }
    }

}
