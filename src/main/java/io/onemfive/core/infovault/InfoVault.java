package io.onemfive.core.infovault;

import io.onemfive.core.Config;
import io.onemfive.core.LifeCycle;
import io.onemfive.core.infovault.neo4j.NEO4JDBManager;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * InfoVault
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
public class InfoVault implements LifeCycle {

    public enum Status {Starting,Running,Shutdown,StartupFailed}

    private static final Logger LOG = Logger.getLogger(InfoVault.class.getName());

    private static InfoVault instance;
    private static final Object lock = new Object();

    private Status status = Status.Shutdown;

    private Properties props;
    private NEO4JDBManager gdbMgr;

    private DIDDAO didDAO;
    private HealthDAO healthDAO;
    private MemoryTestDAO memoryTestDAO;

    private InfoVault(){}

    public static InfoVault getInstance() {
        if(instance == null) {
            synchronized (lock) {
                instance = new InfoVault();
            }
        }
        return instance;
    }

    public DIDDAO getDidDAO() {
        return didDAO;
    }

    public HealthDAO getHealthDAO() {
        return healthDAO;
    }

    public MemoryTestDAO getMemoryTestDAO() {
        return memoryTestDAO;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean start(Properties properties) {
        status = Status.Starting;
        LOG.info("Starting...");
        try {
            props = Config.loadFromClasspath("infovault.config", properties, false);
            gdbMgr = new NEO4JDBManager();
            gdbMgr.start(properties);
            didDAO = new DIDDAO(gdbMgr);
            healthDAO = new HealthDAO(gdbMgr);
            memoryTestDAO = new MemoryTestDAO(gdbMgr);
        } catch (Exception e) {
            status = Status.StartupFailed;
            e.printStackTrace();
            LOG.warning("Failed to start: "+e.getLocalizedMessage());
            return false;
        }
        status = Status.Running;
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
        return false;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        boolean shutdown = gdbMgr.shutdown();
        LOG.info("Shutdown.");
        return shutdown;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
