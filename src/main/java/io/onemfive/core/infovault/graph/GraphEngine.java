package io.onemfive.core.infovault.graph;


import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.data.Peer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

public class GraphEngine {

    private static final Logger LOG = Logger.getLogger(GraphEngine.class.getName());

    private Properties properties;
    private GraphDatabaseService graphDb;

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

    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    public void example() {
        try (Transaction tx = graphDb.beginTx()) {
            Node articleOld = graphDb.createNode();
            articleOld.setProperty("message","Hello, ");
            Node articleNew = graphDb.createNode();
            articleNew.setProperty("message","World!");
            Relationship rel = articleOld.createRelationshipTo(articleNew, RelTypes.KNOWS);
            rel.setProperty("message","brave Neo4j ");
            LOG.info((String)articleOld.getProperty("message")+rel.getProperty("message")+articleNew.getProperty("message"));
            tx.success();
        }
    }

    public boolean teardown() {
        LOG.info("Tearing down...");
        graphDb.shutdown();
        LOG.info("Torn down.");
        return true;
    }

    public static void main(String[] args) {
        GraphEngine graphEngine = new GraphEngine();
        graphEngine.init(null);
        graphEngine.example();
    }
}
