package io.onemfive.core.infovault.graph;


import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.did.dao.LoadDIDDAO;
import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.data.DID;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class GraphEngine {

    private static final Logger LOG = Logger.getLogger(GraphEngine.class.getName());

    private Properties properties;
    private GraphDatabaseService graphDb;

    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    public boolean init(Properties properties) {
        this.properties = properties;
        File baseDir = OneMFiveAppContext.getInstance().getBaseDir();
        File dbDir = new File(baseDir, "/gdb");
        if(!dbDir.exists() && !dbDir.mkdir()) {
            LOG.warning("Unable to create graph db directory.");
            return false;
        }

        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbDir);

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                LOG.info("Stopping...");
                graphDb.shutdown();
            }
        } );

        return true;
    }

    public boolean teardown() {
        LOG.info("Tearing down...");
        graphDb.shutdown();
        LOG.info("Torn down.");
        return true;
    }
}
