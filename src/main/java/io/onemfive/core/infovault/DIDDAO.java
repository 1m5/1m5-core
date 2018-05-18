package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.DID;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.security.SecureRandom;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class DIDDAO {

    private NitriteDBManager dbMgr;
    private SecureRandom random = new SecureRandom(new byte[2398]);

    DIDDAO(NitriteDBManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    public DID createDID(String alias, String passphrase) {
        DID did = load(alias);
        if(did == null) {
            System.out.println(DIDDAO.class.getName()+": Creating DID with alias: "+alias);
            did = new DID();
            did.setId(random.nextLong());
            did.setAlias(alias);
            did.setPassphrase(passphrase);
            did.setStatus(DID.Status.ACTIVE);
            dbMgr.getDb().getRepository(DID.class).insert(did);
        } else {
            System.out.println(DIDDAO.class.getName()+": DID alias already present: "+alias);
        }
        return did;
    }

    public void updateDID(DID did) {
        dbMgr.getDb().getRepository(DID.class).update(did);
    }

    public DID load(String alias) {
        return dbMgr.getDb().getRepository(DID.class)
                .find(ObjectFilters.eq("alias",alias)).firstOrDefault();
    }

}
