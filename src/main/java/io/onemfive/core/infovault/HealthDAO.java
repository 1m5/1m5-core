package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.health.HealthRecord;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
class HealthDAO {

    private NitriteDBManager dbMgr;

    public HealthDAO(NitriteDBManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    HealthRecord loadHealthRecord(Long did) {
        ObjectRepository<HealthRecord> r = dbMgr.getDb().getRepository(HealthRecord.class);
        Cursor<HealthRecord> records = r.find(ObjectFilters.eq("did",did));
        if(records.size() > 0) {
            return records.toList().get(0);
        } else {
            return null;
        }
    }
}
