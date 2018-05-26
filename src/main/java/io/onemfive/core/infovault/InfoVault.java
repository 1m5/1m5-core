package io.onemfive.core.infovault;

import io.onemfive.core.Config;
import io.onemfive.core.LifeCycle;
import io.onemfive.core.infovault.nitrite.NitriteDBManager;

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

    private final Logger LOG = Logger.getLogger(InfoVault.class.getName());

    private static InfoVault instance;
    private static Object lock = new Object();

    private Properties props;
    private NitriteDBManager db;

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

    @Override
    public boolean start(Properties properties) {
        System.out.println("InfoVault starting up...");
        try {
            props = Config.loadFromClasspath("infovault.config", properties);
            db = new NitriteDBManager();
            db.start(properties);

            didDAO = new DIDDAO(db);
            healthDAO = new HealthDAO(db);
            memoryTestDAO = new MemoryTestDAO(db);

        } catch (Exception e) {
            System.out.println("InfoVault failed to start: "+e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        System.out.println("InfoVault started.");
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
        System.out.println("InfoVault shutting down...");
        boolean shutdown = db.shutdown();
        System.out.println("InfoVault shutdown.");
        return shutdown;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
