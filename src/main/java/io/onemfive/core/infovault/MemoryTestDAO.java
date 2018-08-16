package io.onemfive.core.infovault;

import io.onemfive.core.infovault.neo4j.BaseDAO;
import io.onemfive.core.infovault.neo4j.NEO4JDBManager;
import io.onemfive.data.health.mental.memory.MemoryTest;

import java.util.List;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class MemoryTestDAO extends BaseDAO {

    private static final Logger LOG = Logger.getLogger(MemoryTestDAO.class.getName());

    MemoryTestDAO(NEO4JDBManager gdbMgr) {
        super(gdbMgr);
    }

    public void create(MemoryTest memoryTest) {

    }

    public MemoryTest load(Long id) {

        return null;
    }

    public List<MemoryTest> loadListByDID(Long did, int offset, int size) {

        return null;
    }

    public List<MemoryTest> loadListByDID(Long did) {
        LOG.info("LoadListByDID...");

        return null;
    }

}
