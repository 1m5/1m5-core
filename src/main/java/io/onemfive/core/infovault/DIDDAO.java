package io.onemfive.core.infovault;

import io.onemfive.core.infovault.neo4j.BaseDAO;
import io.onemfive.core.infovault.neo4j.NEO4JDBManager;
import io.onemfive.data.DID;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.logging.Logger;

/**
 * DID Data Access Object
 *
 * @author objectorange
 */
public class DIDDAO extends BaseDAO {

    private static final Logger LOG = Logger.getLogger(DIDDAO.class.getName());

    DIDDAO(NEO4JDBManager gdbMgr) {
        super(gdbMgr);
    }

    public void saveDID(DID d) {
        try (Transaction tx = gdb.beginTx()) {
            Node n = gdb.createNode();
            n.getAllProperties().putAll(d.toMap());
            tx.success();
        }
    }

    public DID load(String alias) {

        return null;
    }

}
