package io.onemfive.core.infovault.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;

public abstract class BaseDAO {

    protected NEO4JDBManager gdbMgr;
    protected GraphDatabaseService gdb;

    public BaseDAO(NEO4JDBManager gdbMgr) {
        this.gdbMgr = gdbMgr;
        this.gdb = gdbMgr.getGdb();
    }
}
