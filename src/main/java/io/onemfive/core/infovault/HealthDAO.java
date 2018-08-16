package io.onemfive.core.infovault;

import io.onemfive.core.infovault.neo4j.BaseDAO;
import io.onemfive.core.infovault.neo4j.NEO4JDBManager;
import io.onemfive.data.health.HealthRecord;

import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class HealthDAO extends BaseDAO {

    private static final Logger LOG = Logger.getLogger(HealthDAO.class.getName());

    HealthDAO(NEO4JDBManager gdbMgr) {
        super(gdbMgr);
    }

    public HealthRecord loadHealthRecord(Long did) {

        return null;
    }

    public void saveHealthRecord(HealthRecord healthRecord) {
        LOG.info("Saving Health Record: did="+healthRecord.getDid());


    }
}
