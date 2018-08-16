package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.BaseDAO;
import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.health.mental.memory.MemoryTest;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class MemoryTestDAO extends BaseDAO {

    private static final Logger LOG = Logger.getLogger(MemoryTestDAO.class.getName());

    MemoryTestDAO(NitriteDBManager dbMgr) {
        super(dbMgr);
//        dbMgr.getDb().getRepository(MemoryTest.class).drop();
    }

    public void create(MemoryTest memoryTest) {
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        memoryTest.setId(nextId());
        LOG.info("Saving MemoryTest (id="+memoryTest.getId()+") with DID (id="+memoryTest.getDid()+")");
        r.insert(memoryTest);
        LOG.info("MemoryTest History for DID (id="+memoryTest.getDid()+"): ");
        List<MemoryTest> tests = loadListByDID(memoryTest.getDid());
        for(MemoryTest t : tests) {
            LOG.info(t.toString());
        }
    }

    public MemoryTest load(Long id) {
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        Cursor<MemoryTest> tests = r.find(ObjectFilters.eq("id",id));
        return tests.firstOrDefault();
    }

    public List<MemoryTest> loadListByDID(Long did, int offset, int size) {
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        Cursor<MemoryTest> tests = r.find(ObjectFilters.eq("did",did), FindOptions.sort("timeEnded",SortOrder.Descending).thenLimit(offset, size));
        return tests.toList();
    }

    public List<MemoryTest> loadListByDID(Long did) {
        LOG.info("LoadListByDID...");
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        Cursor<MemoryTest> tests = r.find(ObjectFilters.eq("did",did), FindOptions.sort("timeEnded",SortOrder.Descending));
        LOG.info("Number tests found: "+tests.size());
        return tests.toList();
    }

}
