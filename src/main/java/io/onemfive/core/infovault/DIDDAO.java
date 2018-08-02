package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.DID;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class DIDDAO {

    private static final Logger LOG = Logger.getLogger(DIDDAO.class.getName());

    private NitriteDBManager dbMgr;
    private SecureRandom random = new SecureRandom(new byte[2398]);

    DIDDAO(NitriteDBManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    public DID createDID(String alias, String passphrase, String hashAlgorithm) throws NoSuchAlgorithmException {
        DID did = load(alias);
        if(did == null) {
            LOG.info("Creating DID with alias: "+alias);
            did = DID.create(alias, passphrase, hashAlgorithm);
            dbMgr.getDb().getRepository(DID.class).insert(did);
        } else {
            LOG.info("DID alias already present: "+alias);
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
