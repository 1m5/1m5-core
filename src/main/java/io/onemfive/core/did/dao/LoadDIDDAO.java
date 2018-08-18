package io.onemfive.core.did.dao;

import io.onemfive.core.infovault.BaseDAO;
import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.data.DID;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class LoadDIDDAO extends BaseDAO {

    private DID providedDID;
    private DID loadedDID;

    public LoadDIDDAO(InfoVaultDB infoVaultDB, DID did) {
        super(infoVaultDB);
        this.providedDID = did;
    }

    @Override
    public void execute() {
        try (Transaction tx = infoVaultDB.getGraphEngine().getGraphDb().beginTx()) {
            Node node = infoVaultDB.getGraphEngine().getGraphDb().findNode(Label.label(DID.class.getName()), "alias", providedDID.getAlias());
            if(node != null) {
                loadedDID = new DID();
                loadedDID.fromMap(node.getAllProperties());
            }
        }
    }

    public DID getLoadedDID() {
        return loadedDID;
    }
}
