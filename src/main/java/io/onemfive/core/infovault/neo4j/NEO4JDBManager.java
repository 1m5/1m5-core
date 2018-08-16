package io.onemfive.core.infovault.neo4j;

import io.onemfive.core.LifeCycle;
import io.onemfive.core.OneMFiveAppContext;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

public class NEO4JDBManager implements LifeCycle {

    private static final Logger LOG = Logger.getLogger(NEO4JDBManager.class.getName());

    private static String dbFolder = "/gdb/";
    private String dbFullPath;
    private File dbLocation;

    private GraphDatabaseService gdb;

    public NEO4JDBManager() {

    }

    GraphDatabaseService getGdb() {
        return gdb;
    }

    @Override
    public boolean start(Properties properties) {
        dbFullPath = OneMFiveAppContext.getInstance().getBaseDir()+dbFolder;
        File dbFullPathDir = new File(dbFullPath);
        if(!dbFullPathDir.exists()) {
            if(!dbFullPathDir.mkdir()) {
                LOG.warning("Unable to make directory for NEO4J: "+dbFullPath);
                return false;
            }
        }
        gdb = new GraphDatabaseFactory().newEmbeddedDatabase(dbFullPathDir);
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                gdb.shutdown();
            }
        } );
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
        gdb.shutdown();
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
