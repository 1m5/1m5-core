package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.BaseDAO;
import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.health.HealthRecord;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class HealthDAO extends BaseDAO {

    private static final Logger LOG = Logger.getLogger(HealthDAO.class.getName());

    HealthDAO(NitriteDBManager dbMgr) {
        super(dbMgr);
//        dbMgr.getDb().getRepository(HealthRecord.class).drop();
    }

    public HealthRecord loadHealthRecord(Long did) {
        LOG.info("Loading Health Record: did="+did);
        ObjectRepository<HealthRecord> r = dbMgr.getDb().getRepository(HealthRecord.class);
        Cursor<HealthRecord> records = r.find(ObjectFilters.eq("did",did));
        if(records.size() > 0) {
            LOG.info("Health Record found for did="+did);
            return records.toList().get(0);
        } else {
            LOG.info("Health Record not found for did="+did+"; creating...");
            // Ensure every DID has a HealthRecord
            HealthRecord record = new HealthRecord();
            record.setDid(did);
            saveHealthRecord(record);
            return record;
        }
    }

    public void saveHealthRecord(HealthRecord healthRecord) {
        LOG.info("Saving Health Record: did="+healthRecord.getDid());
        ObjectRepository<HealthRecord> r = dbMgr.getDb().getRepository(HealthRecord.class);
        if(healthRecord.getId() == null) {
            healthRecord.setId(nextId());
            r.insert(healthRecord);
        } else {
            r.update(healthRecord);
        }
    }
}
