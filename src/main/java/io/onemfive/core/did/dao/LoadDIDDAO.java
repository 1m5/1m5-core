package io.onemfive.core.did.dao;

import io.onemfive.core.infovault.BaseDAO;
import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.core.infovault.graph.GraphUtil;
import io.onemfive.data.DID;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class LoadDIDDAO extends BaseDAO {

    private Boolean autoCreate = false;
    private DID providedDID;
    private DID loadedDID;

    public LoadDIDDAO(InfoVaultDB infoVaultDB, DID did, Boolean autoCreate) {
        super(infoVaultDB);
        this.providedDID = did;
        this.autoCreate = autoCreate;
    }

    @Override
    public void execute() {
        try (Transaction tx = infoVaultDB.getGraphDb().beginTx()) {
            Node node = infoVaultDB.getGraphDb().findNode(Label.label(DID.class.getName()), "alias", providedDID.getAlias());
            if(node == null) {
                if(autoCreate) {
                    node = infoVaultDB.getGraphDb().createNode(Label.label(DID.class.getName()));
                    GraphUtil.updateProperties(node, providedDID.toMap());
                    loadedDID = providedDID;
                }
            } else {
                loadedDID = new DID();
                loadedDID.fromMap(node.getAllProperties());
            }
            tx.success();
        }
    }

    public DID getLoadedDID() {
        return loadedDID;
    }
}
