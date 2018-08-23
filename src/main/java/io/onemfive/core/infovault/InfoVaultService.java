package io.onemfive.core.infovault;

import io.onemfive.core.*;
import io.onemfive.data.*;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.logging.Logger;

/**
 * Asynchronous access to persistence.
 * Access to an instance of LocalFileSystemDB (InfoVaultDB) is provided in each Service too (by BaseService)
 * for synchronous access.
 * Developer's choice to which to use on a per-case basis by Services extending BaseService.
 * Clients always use this service as they do not have direct access to InfoVaultDB.
 * Consider using this service for heavier higher-latency work by Services extending BaseService vs using their
 * synchronous access instance in BaseService.
 *
 * InfoVaultDB:
 * Maintain thread-safe.
 * Use directly synchronously.
 * InfoVaultDB instances are singleton by type when instantiated through InfoVaultService.getInstance(String className).
 * Multiple types can be instantiated in parallel, e.g. LocalFileSystemDB and Neo4jDB
 * Pass in class name (including package) to get an instance of it.
 * Make sure your class implements the InfoVaultDB interface.
 * Current implementations:
 *      io.onemfive.core.infovault.LocalFileSystemDB (default)
 *      io.onemfive.infovault.neo4j.Neo4jDB
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    private static final Logger LOG = Logger.getLogger(InfoVaultService.class.getName());

    protected static Map<String,InfoVaultDB> instances = new HashMap<>();
    private static final Object lock = new Object();

    public static final String OPERATION_EXECUTE = "EXECUTE";

    public InfoVaultService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        switch(r.getOperation()) {
            case OPERATION_EXECUTE: {execute(e);break;}
            default: deadLetter(e);
        }
    }

    private void execute(Envelope e) {
        DAO dao = (DAO)DLC.getData(DAO.class, e);
        dao.execute();
    }

    public static InfoVaultDB getInstance(String infoVaultDBClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        InfoVaultDB instance = instances.get(infoVaultDBClass);
        if(instance == null) {
            synchronized (lock) {
                instance = (InfoVaultDB)Class.forName(infoVaultDBClass).newInstance();
                instances.put(infoVaultDBClass,instance);
            }
        }
        return instance;
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

}
