package io.onemfive.core.did.dao;

import io.onemfive.core.infovault.BaseDAO;
import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.core.infovault.graph.GraphUtil;
import io.onemfive.data.DID;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class SaveDIDDAO extends BaseDAO {

    private DID didToSave;

    public SaveDIDDAO(InfoVaultDB infoVaultDB, DID did) {
        super(infoVaultDB);
        this.didToSave = did;
    }

    @Override
    public void execute() {
        try (Transaction tx = infoVaultDB.getGraphDb().beginTx()) {
            Node node = infoVaultDB.getGraphDb().createNode(Label.label(DID.class.getName()));
            GraphUtil.updateProperties(node, didToSave.toMap());
            tx.success();
        }
    }
}
