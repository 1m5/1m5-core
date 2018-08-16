package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.BaseDAO;
import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.DID;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.logging.Logger;

/**
 * DID Data Access Object
 *
 * @author objectorange
 */
public class DIDDAO extends BaseDAO {

    private static final Logger LOG = Logger.getLogger(DIDDAO.class.getName());

    DIDDAO(NitriteDBManager dbMgr) {
        super(dbMgr);
//        dbMgr.getDb().getRepository(DID.class).drop();
    }

    public void saveDID(DID d) {
        DID did = load(d.getAlias());
        if(did == null) {
            d.setId(nextId());
            dbMgr.getDb().getRepository(DID.class).insert(d);
        } else {
            if(d.getId() == null) d.setId(did.getId());
            dbMgr.getDb().getRepository(DID.class).update(d);
        }
    }

    public DID load(String alias) {
        return dbMgr.getDb().getRepository(DID.class)
                .find(ObjectFilters.eq("alias",alias)).firstOrDefault();
    }

}
