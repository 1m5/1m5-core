package io.onemfive.core.infovault;

import io.onemfive.core.Config;
import io.onemfive.core.infovault.graph.GraphEngine;
import io.onemfive.core.infovault.storage.StorageEngine;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * InfoVaultDB
 *
 * Stores personal information securely while allowing access
 * by other parties with personal approval.
 *
 * Use directly synchronously.
 *
 * Instance is singleton. Maintain thread-safe.
 *
 * @author objectorange
 */
public class InfoVaultDB {

    public enum Status {Starting,Running,Shutdown,StartupFailed}

    private static final Logger LOG = Logger.getLogger(InfoVaultDB.class.getName());

    private static InfoVaultDB instance;
    private static final Object lock = new Object();

    private Status status = Status.Shutdown;

    private Properties props;

    private GraphEngine graphEngine;
    private StorageEngine storageEngine;

    private boolean initialized = false;

    private InfoVaultDB(){
    }

    public static InfoVaultDB getInstance() {
        if(instance == null) {
            synchronized (lock) {
                instance = new InfoVaultDB();
            }
        }
        return instance;
    }

    public GraphEngine getGraphEngine() {
        return graphEngine;
    }

    public GraphDatabaseService getGraphDb() {
        return graphEngine.getGraphDb();
    }

    public StorageEngine getStorageEngine() {
        return storageEngine;
    }

    public Status getStatus() {
        return status;
    }

    public boolean init(Properties properties) {
        if(!initialized) {
            status = Status.Starting;
            LOG.info("Initializing...");
            try {
                props = Config.loadFromClasspath("infovault.config", properties, false);
                graphEngine = new GraphEngine();
                graphEngine.init(properties);
                storageEngine = new StorageEngine();
                storageEngine.init(properties);
            } catch (Exception e) {
                status = Status.StartupFailed;
                e.printStackTrace();
                LOG.warning("Failed to init: " + e.getLocalizedMessage());
                return false;
            }
            status = Status.Running;
            LOG.info("Initialized.");
            initialized = true;
        }
        return true;
    }

    public boolean teardown() {
        LOG.info("Tearing down...");
        graphEngine.teardown();
        storageEngine.teardown();
        LOG.info("Torn down.");
        return true;
    }

}
