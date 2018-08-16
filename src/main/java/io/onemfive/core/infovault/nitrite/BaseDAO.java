package io.onemfive.core.infovault.nitrite;

import java.util.logging.Logger;

public abstract class BaseDAO {

    private static final Logger LOG = Logger.getLogger(BaseDAO.class.getName());

    protected NitriteDBManager dbMgr;

    protected BaseDAO(NitriteDBManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    protected long nextId() {
        LOG.info("Getting next ID for DAO: "+this.getClass().getName());
        return dbMgr.nextId(this.getClass().getName());
    }
}
