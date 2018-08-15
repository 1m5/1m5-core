package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.DID;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * DID Data Access Object
 *
 * @author objectorange
 */
public class DIDDAO {

    private static final Logger LOG = Logger.getLogger(DIDDAO.class.getName());

    private NitriteDBManager dbMgr;

    DIDDAO(NitriteDBManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    public void saveDID(DID d) throws NoSuchAlgorithmException {
        DID did = load(d.getAlias());
        if(did == null) {
            dbMgr.getDb().getRepository(DID.class).insert(d);
        } else {
            dbMgr.getDb().getRepository(DID.class).update(d);
        }
    }

    public DID load(String alias) {
        return dbMgr.getDb().getRepository(DID.class)
                .find(ObjectFilters.eq("alias",alias)).firstOrDefault();
    }

}
