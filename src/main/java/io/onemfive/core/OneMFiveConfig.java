package io.onemfive.core;

import io.onemfive.core.util.SecureFile;
import io.onemfive.core.util.SystemVersion;
import net.i2p.crypto.KeyStoreUtil;
import net.i2p.data.DataHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class OneMFiveConfig {
    public static final String KEY_DERIVATION_PARAMETERS_FILE = "derivparams";   // name of the KDF parameter cache file, relative to I2P_BOTE_SUBDIR

    private static final String I2P_BOTE_SUBDIR = "i2pbote";       // relative to the I2P app dir
    private static final String CONFIG_FILE_NAME = "1m5.config";
    private static final String DEST_KEY_FILE_NAME = "local_dest.key";
    private static final String DHT_PEER_FILE_NAME = "dht_peers.txt";
    private static final String RELAY_PEER_FILE_NAME = "relay_peers.txt";
    private static final String IDENTITIES_FILE_NAME = "identities";
    private static final String ADDRESS_BOOK_FILE_NAME = "addressBook";
    private static final String MESSAGE_ID_CACHE_FILE = "msgidcache.txt";
    private static final String PASSWORD_FILE = "password";
    private static final String SSL_KEYSTORE_FILE = "i2p.bote.ssl.keystore.jks";      // relative to I2P_BOTE_SUBDIR
    private static final String SSL_KEY_ALIAS = "botessl";
    private static final String OUTBOX_DIR = "outbox";              // relative to I2P_BOTE_SUBDIR
    private static final String RELAY_PKT_SUBDIR = "relay_pkt";     // relative to I2P_BOTE_SUBDIR
    private static final String INCOMPLETE_SUBDIR = "incomplete";   // relative to I2P_BOTE_SUBDIR
    private static final String EMAIL_DHT_SUBDIR = "dht_email_pkt";    // relative to I2P_BOTE_SUBDIR
    private static final String INDEX_PACKET_DHT_SUBDIR = "dht_index_pkt";    // relative to I2P_BOTE_SUBDIR
    private static final String DIRECTORY_ENTRY_DHT_SUBDIR = "dht_directory_pkt";    // relative to I2P_BOTE_SUBDIR
    private static final String INBOX_SUBDIR = "inbox";             // relative to I2P_BOTE_SUBDIR
    private static final String SENT_FOLDER_DIR = "sent";           // relative to I2P_BOTE_SUBDIR
    private static final String TRASH_FOLDER_DIR = "trash";         // relative to I2P_BOTE_SUBDIR
    private static final String MIGRATION_VERSION_FILE = "migratedVersion";   // relative to I2P_BOTE_SUBDIR

    // Parameter names in the config file
    private static final String PARAMETER_STORAGE_SPACE_INBOX = "storageSpaceInbox";
    private static final String PARAMETER_STORAGE_SPACE_RELAY = "storageSpaceRelay";
    private static final String PARAMETER_STORAGE_TIME = "storageTime";
    private static final String PARAMETER_HASHCASH_STRENGTH = "hashCashStrength";
    private static final String PARAMETER_SSL_KEYSTORE_PASSWORD = "sslKeystorePassword";
    private static final String PARAMETER_RELAY_SEND_PAUSE = "RelaySendPause";
    private static final String PARAMETER_HIDE_LOCALE = "hideLocale";
    private static final String PARAMETER_INCLUDE_SENT_TIME = "includeSentTime";
    private static final String PARAMETER_MESSAGE_ID_CACHE_SIZE = "messageIdCacheSize";
    private static final String PARAMETER_RELAY_REDUNDANCY = "relayRedundancy";
    private static final String PARAMETER_RELAY_MIN_DELAY = "relayMinDelay";
    private static final String PARAMETER_RELAY_MAX_DELAY = "relayMaxDelay";
    private static final String PARAMETER_NUM_STORE_HOPS = "numSendHops";
    private static final String PARAMETER_PASSWORD_CACHE_DURATION = "passwordCacheDuration";
    private static final String PARAMETER_UPDATE_URL = "updateUrl";
    private static final String PARAMETER_UPDATE_CHECK_INTERVAL = "updateCheckInterval";

    // Defaults for each parameter
    private static final int DEFAULT_STORAGE_SPACE_INBOX = 1024 * 1024 * 1024;
    private static final int DEFAULT_STORAGE_SPACE_RELAY = 100 * 1024 * 1024;
    private static final int DEFAULT_STORAGE_TIME = 31;   // in days
    private static final int DEFAULT_HASHCASH_STRENGTH = 10;
    private static final int DEFAULT_RELAY_SEND_PAUSE = 10;   // in minutes, see RelayPacketSender.java
    private static final boolean DEFAULT_HIDE_LOCALE = true;
    private static final boolean DEFAULT_INCLUDE_SENT_TIME = true;
    private static final int DEFAULT_MESSAGE_ID_CACHE_SIZE = 1000;   // the maximum number of message IDs to cache
    private static final int DEFAULT_RELAY_REDUNDANCY = 5;   // lower than the DHT redundancy because only the highest-uptime peers are used for relaying
    private static final int DEFAULT_RELAY_MIN_DELAY = 5;   // in minutes
    private static final int DEFAULT_RELAY_MAX_DELAY = 40;   // in minutes
    private static final int DEFAULT_NUM_STORE_HOPS = 0;
    private static final int DEFAULT_PASSWORD_CACHE_DURATION = 10;   // in minutes
    private static final String DEFAULT_UPDATE_ADDRESS = "amifjp8mj3jajd3fjfo3jiaffjdsffawefkewpfo";
    private static final int DEFAULT_UPDATE_CHECK_INTERVAL = 60;   // in minutes
    private static final String DEFAULT_THEME = "material";

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

    private Logger LOG = Logger.getLogger(OneMFiveConfig.class.getName());
    private Properties properties;
    private File baseDir;
    private File configFile;

    /**
     * Reads configuration settings from the <code>BASE_DIR</code> subdirectory under
     * the I2P application directory. The I2P application directory can be changed via the
     * <code>i2p.dir.app</code> system property.
     * <p/>
     * Logging is done through the I2P logger. I2P reads the log configuration from the
     * <code>logger.config</code> file whose location is determined by the
     * <code>i2p.dir.config</code> system property.
     */
    public OneMFiveConfig() {
        properties = new Properties();

        // get the I2PBote directory and make sure it exists
        baseDir = getBaseDirectory();
        if (!baseDir.exists() && !baseDir.mkdirs())
            LOG.warning("Cannot create directory: <" + baseDir.getAbsolutePath() + ">");

        // read the configuration file
        configFile = new File(baseDir, CONFIG_FILE_NAME);
        boolean configurationLoaded = false;
        if (configFile.exists()) {
            LOG.info("Loading config file <" + configFile.getAbsolutePath() + ">");

            try {
                DataHelper.loadProps(properties, configFile);
                configurationLoaded = true;
            } catch (IOException e) {
                LOG.warning("Error loading configuration file <" + configFile.getAbsolutePath() + ">: " + e.getLocalizedMessage());
            }
        }
        if (!configurationLoaded)
            LOG.info("Can't read configuration file <" + configFile.getAbsolutePath() + ">, using default settings.");

        // Create SSL key if necessary
        if (!SystemVersion.isAndroid()) {
            File ks = getSSLKeyStoreFile();
            if (!ks.exists())
                createKeyStore(ks);
        }
    }

    private boolean createKeyStore(File ks) {
        // make a random 48 character password (30 * 8 / 5)
        String keyStorePassword = KeyStoreUtil.randomString();
        // and one for the cname
        String cname = KeyStoreUtil.randomString() + ".ssl.1m5";

        boolean success = KeyStoreUtil.createKeys(
                ks, keyStorePassword, SSL_KEY_ALIAS, cname, "1m5",
                3652, "RSA", 2048, keyStorePassword);
        if (success) {
            success = ks.exists();
            if (success) {
                properties.setProperty(PARAMETER_SSL_KEYSTORE_PASSWORD, keyStorePassword);
                save();
            }
        }
        if (success) {
            LOG.info("Created self-signed certificate for " + cname + " in keystore: " + ks.getAbsolutePath() + "\n" +
                    "The certificate name was generated randomly, and is not associated with your " +
                    "IP address, host name, router identity, or destination keys.");
        } else {
            LOG.warning("Failed to create I2P-Bote SSL keystore.\n" +
                    "This is for the Sun/Oracle keytool, others may be incompatible.\n" +
                    "If you create the keystore manually, you must add " + PARAMETER_SSL_KEYSTORE_PASSWORD +
                    " to " + (new File(baseDir, CONFIG_FILE_NAME)).getAbsolutePath() + "\n" +
                    "You must create the keystore using the same password for the keystore and the key.");
        }
        return success;
    }

    /**
     * @param name
     */
    public void setI2CPDomainSocket(String name) {
        if (SystemVersion.isAndroid())
            properties.setProperty(
                    PARAMETER_I2CP_DOMAIN_SOCKET, name);
    }

    /**
     * @return a Properties containing the current I2CP options.
     */
    public Properties getI2CPOptions() {
        Properties opts = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (I2CP_PARAMETERS.contains(entry.getKey()))
                opts.put(entry.getKey(), entry.getValue());
        }
        return opts;
    }

    public File getDestinationKeyFile() {
        return new File(baseDir, DEST_KEY_FILE_NAME);
    }

    public File getDhtPeerFile() {
        return new File(baseDir, DHT_PEER_FILE_NAME);
    }

    public File getRelayPeerFile() {
        return new File(baseDir, RELAY_PEER_FILE_NAME);
    }

    public File getIdentitiesFile() {
        return new File(baseDir, IDENTITIES_FILE_NAME);
    }

    public File getAddressBookFile() {
        return new File(baseDir, ADDRESS_BOOK_FILE_NAME);
    }

    public File getMessageIdCacheFile() {
        return new File(baseDir, MESSAGE_ID_CACHE_FILE);
    }

    /**
     * The file returned by this method does not contain the user's password,
     * but a known string that is encrypted with the password. The purpose
     * of this file is for checking if a password entered by the user is
     * correct.
     */
    public File getPasswordFile() {
        return new File(baseDir, PASSWORD_FILE);
    }

    /**
     * Returns the file that caches the parameters needed for generating a
     * file encryption key from a password.
     */
    public File getKeyDerivationParametersFile() {
        return new File(baseDir, KEY_DERIVATION_PARAMETERS_FILE);
    }

    /**
     * @return the keystore file containing the SSL server key.
     */
    public File getSSLKeyStoreFile() {
        return new File(baseDir, SSL_KEYSTORE_FILE);
    }

    private static File getBaseDirectory() {
        // the parent directory of the I2PBote directory ($HOME or the value of the i2p.dir.app property)
        File i2pAppDir = OneMFiveAppContext.getInstance().getAppDir();

        return new File(i2pAppDir, I2P_BOTE_SUBDIR);
    }

    /**
     * Saves the configuration to a file.
     */
    public void save() {
        LOG.info("Saving config file <" + configFile.getAbsolutePath() + ">");
        try {
            DataHelper.storeProps(properties, new SecureFile(configFile.getAbsolutePath()));
        } catch (IOException e) {
            LOG.warning("Cannot save configuration to file <" + configFile.getAbsolutePath() + ">: " + e.getLocalizedMessage());
        }
    }

    /**
     * Returns the maximum size (in bytes) the inbox can take up.
     */
    public int getStorageSpaceInbox() {
        return getIntParameter(PARAMETER_STORAGE_SPACE_INBOX, DEFAULT_STORAGE_SPACE_INBOX);
    }

    /**
     * Returns the maximum size (in bytes) all messages stored for relaying can take up.
     */
    public int getStorageSpaceRelay() {
        return getIntParameter(PARAMETER_STORAGE_SPACE_RELAY, DEFAULT_STORAGE_SPACE_RELAY);
    }

    /**
     * Returns the time (in milliseconds) after which content is deleted from the outbox if it cannot be sent or relayed.
     */
    public long getStorageTime() {
        return 24L * 3600 * 1000 * getIntParameter(PARAMETER_STORAGE_TIME, DEFAULT_STORAGE_TIME);
    }

    public int getHashCashStrength() {
        return getIntParameter(PARAMETER_HASHCASH_STRENGTH, DEFAULT_HASHCASH_STRENGTH);
    }

    /**
     * @return the password for the SSL keystore.
     */
    public String getSSLKeyStorePassword() {
        return properties.getProperty(PARAMETER_SSL_KEYSTORE_PASSWORD);
    }

    public void setRelaySendPause(int minutes) {
        properties.setProperty(PARAMETER_RELAY_SEND_PAUSE, String.valueOf(minutes));
    }

    /**
     * Returns the number of minutes to wait before processing the relay packet folder again.
     */
    public int getRelaySendPause() {
        return getIntParameter(PARAMETER_RELAY_SEND_PAUSE, DEFAULT_RELAY_SEND_PAUSE);
    }

    /**
     * Controls whether strings that are added to outgoing email, like "Re:" or "Fwd:",
     * are translated or not.<br/>
     * If <code>hideLocale</code> is <code>false</code>, the UI language is used.<br/>
     * If <code>hideLocale</code> is <code>true</code>, the strings are left untranslated
     * (which means they are in English).
     * @param hideLocale
     */
    public void setHideLocale(boolean hideLocale) {
        properties.setProperty(PARAMETER_HIDE_LOCALE, String.valueOf(hideLocale));
    }

    public boolean getHideLocale() {
        return getBooleanParameter(PARAMETER_HIDE_LOCALE, DEFAULT_HIDE_LOCALE);
    }

    /**
     * Controls whether the send time is included in outgoing emails.
     * @param includeSentTime
     */
    public void setIncludeSentTime(boolean includeSentTime) {
        properties.setProperty(PARAMETER_INCLUDE_SENT_TIME, String.valueOf(includeSentTime));
    }

    public boolean getIncludeSentTime() {
        return getBooleanParameter(PARAMETER_INCLUDE_SENT_TIME, DEFAULT_INCLUDE_SENT_TIME);
    }

    public int getMessageIdCacheSize() {
        return getIntParameter(PARAMETER_MESSAGE_ID_CACHE_SIZE, DEFAULT_MESSAGE_ID_CACHE_SIZE);
    }

    /**
     * Returns the number of relay chains that should be used per Relay Request.
     */
    public int getRelayRedundancy() {
        return getIntParameter(PARAMETER_RELAY_REDUNDANCY, DEFAULT_RELAY_REDUNDANCY);
    }

    public void setRelayMinDelay(int minDelay) {
        properties.setProperty(PARAMETER_RELAY_MIN_DELAY, String.valueOf(minDelay));
    }

    /**
     * Returns the minimum amount of time in minutes that a Relay Request is delayed.
     */
    public int getRelayMinDelay() {
        return getIntParameter(PARAMETER_RELAY_MIN_DELAY, DEFAULT_RELAY_MIN_DELAY);
    }

    public void setRelayMaxDelay(int maxDelay) {
        properties.setProperty(PARAMETER_RELAY_MAX_DELAY, String.valueOf(maxDelay));
    }

    /**
     * Returns the maximum amount of time in minutes that a Relay Request is delayed.
     */
    public int getRelayMaxDelay() {
        return getIntParameter(PARAMETER_RELAY_MAX_DELAY, DEFAULT_RELAY_MAX_DELAY);
    }

    public void setNumStoreHops(int numHops) {
        properties.setProperty(PARAMETER_NUM_STORE_HOPS, String.valueOf(numHops));
    }

    /**
     * Returns the number of relays that should be used when sending a DHT store request.
     * @return A non-negative number
     */
    public int getNumStoreHops() {
        return getIntParameter(PARAMETER_NUM_STORE_HOPS, DEFAULT_NUM_STORE_HOPS);
    }

    public void setPasswordCacheDuration(int duration) {
        properties.setProperty(PARAMETER_PASSWORD_CACHE_DURATION, String.valueOf(duration));
    }

    /**
     * Returns the number of minutes the password is kept in memory
     */
    public int getPasswordCacheDuration() {
        return getIntParameter(PARAMETER_PASSWORD_CACHE_DURATION, DEFAULT_PASSWORD_CACHE_DURATION);
    }

    /**
     * Returns an Address pointing to the Peer with an update file.
     */
    public String getUpdateUrl() {
        return properties.getProperty(PARAMETER_UPDATE_URL, DEFAULT_UPDATE_ADDRESS);
    }

    /**
     * Returns the number of minutes to wait after checking for a new plugin version.
     */
    public int getUpdateCheckInterval() {
        return getIntParameter(PARAMETER_UPDATE_CHECK_INTERVAL, DEFAULT_UPDATE_CHECK_INTERVAL);
    }

    private boolean getBooleanParameter(String parameterName, boolean defaultValue) {
        try {
            return Util.getBooleanParameter(properties, parameterName, defaultValue);
        } catch (IllegalArgumentException e) {
            LOG.warning("getBooleanParameter failed, using default: " + e.getLocalizedMessage());
            return defaultValue;
        }
    }

    private int getIntParameter(String parameterName, int defaultValue) {
        try {
            return Util.getIntParameter(properties, parameterName, defaultValue);
        } catch (NumberFormatException e) {
            LOG.warning("getIntParameter failed, using default: " + e.getLocalizedMessage());
            return defaultValue;
        }
    }
}
