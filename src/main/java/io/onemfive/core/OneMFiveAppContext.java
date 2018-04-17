package io.onemfive.core;

import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.core.util.data.Base64;
import io.onemfive.core.util.*;
import io.onemfive.core.util.stat.StatManager;

import java.io.File;
import java.util.*;

/**
 * <p>Provide a scope for accessing services that SC provides.  Rather than
 * using the traditional singleton, where any component can access the component
 * in question directly, all of those SC related services are exposed through
 * a particular OneMFiveAppContext. This helps not only with understanding their use
 * and the services SC provides, but it also allows multiple isolated
 * environments to operate concurrently within the same JVM - particularly useful
 * for stubbing out implementations of the rooted services and simulating the
 * software's interaction between multiple instances.</p>
 *
 * As a simplification, there is also a global context - if some component needs
 * access to one of the services but doesn't have its own context from which
 * to root itself, it binds to the OneMFiveAppContext's globalAppContext(), which is
 * the first context that was created within the JVM, or a new one if no context
 * existed already.  This functionality is often used within the SC core for
 * logging - e.g. <pre>
 *     private static final Log _log = new Log(someClass.class);
 * </pre>
 * It is for this reason that applications that care about working with multiple
 * contexts should build their own context as soon as possible (within the main(..))
 * so that any referenced components will latch on to that context instead of
 * instantiating a new one.  However, there are situations in which both can be
 * relevant.
 *
 * @author I2P, objectorange
 */
public class OneMFiveAppContext {
    /** the context that components without explicit root are bound */
    protected static OneMFiveAppContext globalAppContext;

    protected final Properties overrideProps;

    private StatManager statManager;
    private LogManager logManager;
    private SimpleTimer simpleTimer;

    private ServiceBus serviceBus;

    private volatile boolean statManagerInitialized;
    private volatile boolean logManagerInitialized;
    private volatile boolean simpleTimerInitialized;
    protected volatile boolean clockInitialized; // used in ConsciousContext
    private volatile boolean randomInitialized;
    private volatile boolean shaInitialized;


    protected final Set<Runnable> shutdownTasks;
    private final File baseDir;
    private final File configDir;
    private final File consciousDir;
    private final File pidDir;
    private final File logDir;
    private final File appDir;
    private volatile File tmpDir;
    private final Random tmpDirRand = new Random();
    private static ClientAppManager clientAppManager;
    private final static Object lockA = new Object();
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
    public static OneMFiveAppContext getInstance() {
        if(globalAppContext == null) {
            synchronized (lockA) {
                globalAppContext = new OneMFiveAppContext(false, null);
            }
        }
        System.out.println(OneMFiveAppContext.class.getSimpleName()+": returning instance: "+globalAppContext);
        return globalAppContext;
    }

    public static OneMFiveAppContext getInstance(Properties properties) {
        if(globalAppContext == null) {
            synchronized (lockA) {
                globalAppContext = new OneMFiveAppContext(false, properties);
            }
        }
        System.out.println(OneMFiveAppContext.class.getSimpleName()+": returning instance: "+globalAppContext);
        return globalAppContext;
    }

    public static void clearGlobalContext() {
        globalAppContext = null;
    }

    /**
     * Create a brand new context.
     * WARNING: In almost all cases, you should use getGlobalContext() instead,
     * to avoid creating additional contexts, which may spawn numerous
     * additional resources and threads, and may be the cause of logging
     * problems or hard-to-isolate bugs.
     */
    public OneMFiveAppContext() {
        this(true, null);
    }

    /**
     * Create a brand new context.
     * WARNING: In almost all cases, you should use getGlobalContext() instead,
     * to avoid creating additional contexts, which may spawn numerous
     * additional resources and threads, and may be the cause of logging
     * problems or hard-to-isolate bugs.
     */
    public OneMFiveAppContext(java.util.Properties envProps) {
        this(true, envProps);
    }

    /**
     * Create a brand new context.
     * WARNING: In almost all cases, you should use getGlobalContext() instead,
     * to avoid creating additional contexts, which may spawn numerous
     * additional resources and threads, and may be the cause of logging
     * problems or hard-to-isolate bugs.
     *
     * @param doInit should this context be used as the global one (if necessary)?
     *               Will only apply if there is no global context now.
     */
    private OneMFiveAppContext(boolean doInit, java.util.Properties envProps) {
        overrideProps = new Properties();
        if (envProps != null)
            overrideProps.putAll(envProps);
        shutdownTasks = new ConcurrentHashSet<>(10);
    /*
    *  Directories. These are all set at instantiation and will not be changed by
    *  subsequent property changes.
    *  All properties, if set, should be absolute paths.
    *
    *  Name	Property 	Method		Files
    *  -----	-------- 	-----		-----
    *  Base	sc.dir.base	getBaseDir()	lib/, webapps/, docs/, geoip/, licenses/, ...
    *  Temp	sc.dir.temp	getTempDir()	Temporary files
    *  Config sc.dir.config	getConfigDir()	*.config, hosts.txt, addressbook/, ...
    *
    *  (the following all default to the same as Config)
    *
    *  PID	sc.dir.pid	getPIDDir()	router.ping
    *  Router	sc.dir.router	getRouterDir()	netDb/, peerProfiles/, router.*, keyBackup/, ...
    *  Log	sc.dir.log	getLogDir()	logs/
    *  App	sc.dir.app	getAppDir()	eepsite/, ...
    *
    *  Note that we can't control where the wrapper puts its files.
    *
    *  The app dir is where all data files should be. Apps should always read and write files here,
    *  using a constructor such as:
    *
    *       String path = mypath;
    *       File f = new File(path);
    *       if (!f.isAbsolute())
    *           f = new File(_context.geAppDir(), path);
    *
    *  and never attempt to access files in the CWD using
    *
    *       File f = new File("foo");
    *
    *  An app should assume the CWD is not writable.
    *
    *  Here in OneMFiveAppContext, all the dirs default to CWD.
    *  However these will be different in ConsciousContext, as ConsciousService.java will set
    *  the properties in the ConsciousContext constructor.
    *
    *  Apps should never need to access the base dir, which is the location of the base SC install.
    *  However this is provided for the Conscious' use, and for backward compatibility should an app
    *  need to look there as well.
    *
    *  All dirs except the base are created if they don't exist, but the creation will fail silently.
    */

        String s = getProperty("sc.dir.base", System.getProperty("user.dir"));
        System.out.println("Base Directory: "+s);
        baseDir = new File(s);

        // config defaults to base
        s = getProperty("sc.dir.config");
        if (s != null) {
            configDir = new SecureFile(s);
            if (!configDir.exists())
                configDir.mkdir();
        } else {
            configDir = baseDir;
        }

        // conscious defaults to config
        s = getProperty("sc.dir.conscious");
        if (s != null) {
            consciousDir = new SecureFile(s);
            if (!consciousDir.exists())
                consciousDir.mkdir();
        } else {
            consciousDir = configDir;
        }

        // pid defaults to router directory (as of 0.8.12, was system temp dir previously)
        s = getProperty("sc.dir.pid");
        if (s != null) {
            pidDir = new SecureFile(s);
            if (!pidDir.exists())
                pidDir.mkdir();
        } else {
            pidDir = consciousDir;
        }

        // these all default to router
        s = getProperty("sc.dir.log");
        if (s != null) {
            logDir = new SecureFile(s);
            if (!logDir.exists())
                logDir.mkdir();
        } else {
            logDir = consciousDir;
        }

        s = getProperty("sc.dir.app");
        if (s != null) {
            appDir = new SecureFile(s);
            if (!appDir.exists())
                appDir.mkdir();
        } else {
            appDir = consciousDir;
        }

        clientAppManager = new ClientAppManager(false);
        // Instantiate Service Bus
        serviceBus = new ServiceBus(overrideProps, clientAppManager);

        /******
         (new Exception("Initialized by")).printStackTrace();
         System.err.println("Base directory:   " + baseDir.getAbsolutePath());
         System.err.println("Config directory: " + configDir.getAbsolutePath());
         System.err.println("Conscious directory: " + consciousDir.getAbsolutePath());
         System.err.println("App directory:    " + appDir.getAbsolutePath());
         System.err.println("Log directory:    " + logDir.getAbsolutePath());
         System.err.println("PID directory:    " + pidDir.getAbsolutePath());
         System.err.println("Temp directory:   " + getTempDir().getAbsolutePath());
         ******/

        if (doInit) {
            if (globalAppContext == null) {
                globalAppContext = this;
            } else {
                System.out.println("Warning - New context not replacing old one, you now have an additional one");
                (new Exception("I did it")).printStackTrace();
            }
        }
    }

    public ClientAppManager getClientAppManager() {
        if(clientAppManager.getStatus() == ClientAppManager.Status.STOPPED)
            clientAppManager.initialize();
        return clientAppManager;
    }

    public ServiceBus getServiceBus() {
        return serviceBus;
    }

    /**
     *  This is the installation dir, often referred to as $SC.
     *  Applications should consider this directory read-only and never
     *  attempt to write to it.
     *  It may actually be read-only on a multi-user installation.
     *  The config files in this directory are templates for user
     *  installations and should not be accessed by applications.
     *  The only thing that may be useful in here is the lib/ dir
     *  containing the .jars.
     *
     *  @return dir constant for the life of the context
     */
    public File getBaseDir() { return baseDir; }

    /**
     *  The base dir for config files.
     *  Applications may use this to access router configuration files if necessary.
     *  Usually ~/.sc on Linux and %APPDIR%\SC on Windows.
     *
     *  @return dir constant for the life of the context
     */
    public File getConfigDir() { return configDir; }

    /**
     *  Where the Conscious keeps its files.
     *  Applications should not use this.
     *  The same as the config dir for now.
     *
     *  @return dir constant for the life of the context
     */
    public File getConsciousDir() { return consciousDir; }

    /**
     *  Where conscious.ping goes.
     *  Applications should not use this.
     *
     *  @return dir constant for the life of the context
     */
    public File getPIDDir() { return pidDir; }

    /**
     *  Where the conscious keeps its log directory.
     *  Applications should not use this.
     *  (i.e. ~/.sc, NOT ~/.sc/logs)
     *
     *  @return dir constant for the life of the context
     */
    public File getLogDir() { return logDir; }

    /**
     *  Where applications may store data.
     *  The same as the config dir for now, but may change in the future.
     *  Apps should be careful not to overwrite conscious files.
     *
     *  @return dir constant for the life of the context
     */
    public File getAppDir() { return appDir; }

    /**
     *  Where anybody may store temporary data.
     *  This is a directory created in the system temp dir on the
     *  first call in this context, and is deleted on JVM exit.
     *  Applications should create their own directory inside this directory
     *  to avoid collisions with other apps.
     *
     *  @return dir constant for the life of the context
     */
    public File getTempDir() {
        // fixme don't synchronize every time
        synchronized (lock1) {
            if (tmpDir == null) {
                String d = getProperty("sc.dir.temp", System.getProperty("java.io.tmpdir"));
                // our random() probably isn't warmed up yet
                byte[] rand = new byte[6];
                tmpDirRand.nextBytes(rand);
                String f = "sc-" + Base64.encode(rand) + ".tmp";
                tmpDir = new SecureFile(d, f);
                if (tmpDir.exists()) {
                    // good or bad ? loop and try again?
                } else if (tmpDir.mkdir()) {
                    tmpDir.deleteOnExit();
                } else {
                    System.err.println("WARNING: Could not create temp dir " + tmpDir.getAbsolutePath());
                    tmpDir = new SecureFile(consciousDir, "tmp");
                    tmpDir.mkdirs();
                    if (!tmpDir.exists())
                        System.err.println("ERROR: Could not create temp dir " + tmpDir.getAbsolutePath());
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
        if (overrideProps != null) {
            String rv = overrideProps.getProperty(propName);
            if (rv != null)
                return rv;
        }
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
        if (overrideProps != null) {
            if (overrideProps.containsKey(propName))
                return overrideProps.getProperty(propName, defaultValue);
        }
        return System.getProperty(propName, defaultValue);
    }

    /**
     * Return an int with an int default
     */
    public int getProperty(String propName, int defaultVal) {
        String val = null;
        if (overrideProps != null) {
            val = overrideProps.getProperty(propName);
            if (val == null)
                val = System.getProperty(propName);
        }
        int ival = defaultVal;
        if (val != null) {
            try {
                ival = Integer.parseInt(val);
            } catch (NumberFormatException nfe) {}
        }
        return ival;
    }

    /**
     * Return a long with a long default
     */
    public long getProperty(String propName, long defaultVal) {
        String val = null;
        if (overrideProps != null) {
            val = overrideProps.getProperty(propName);
            if (val == null)
                val = System.getProperty(propName);
        }
        long rv = defaultVal;
        if (val != null) {
            try {
                rv = Long.parseLong(val);
            } catch (NumberFormatException nfe) {}
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
     *  at shutdown.
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
