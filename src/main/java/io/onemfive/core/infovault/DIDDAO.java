package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.DID;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
class DIDDAO {

    private NitriteDBManager dbMgr;

    DIDDAO(NitriteDBManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    DID createDID(String alias, String passphrase) {
        ObjectRepository<DID> r = dbMgr.getDb().getRepository(DID.class);
        DID did = load(alias);
        if(did == null) {
            did = new DID();
            did.setAlias(alias);
            did.setPassphrase(passphrase);
            did.setStatus(DID.Status.ACTIVE);
            r.insert(did);
        } else {
            System.out.println(DIDDAO.class.getName()+": DID alias already present: "+alias);
        }
        return did;
    }

    void updateDID(DID did) {
        ObjectRepository<DID> r = dbMgr.getDb().getRepository(DID.class);
        r.update(did);
    }

    DID load(String alias) {
        ObjectRepository<DID> r = dbMgr.getDb().getRepository(DID.class);
        Cursor<DID> dids = r.find(ObjectFilters.eq("alias",alias));
        if(dids.size() > 0) {
            return dids.toList().get(0);
        } else {
            return null;
        }
    }

}
