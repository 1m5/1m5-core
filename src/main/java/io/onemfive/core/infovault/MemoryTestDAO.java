package io.onemfive.core.infovault;

import io.onemfive.core.infovault.nitrite.NitriteDBManager;
import io.onemfive.data.health.mental.memory.MemoryTest;
import org.dizitart.no2.Document;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class MemoryTestDAO {

    private NitriteDBManager dbMgr;

    MemoryTestDAO(NitriteDBManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    public void create(MemoryTest memoryTest) {
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        memoryTest.setId(new Random(98473249837442L).nextLong());
        r.insert(memoryTest);
    }

    public List<MemoryTest> loadListByDID(Long did, int offset, int size) {
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        Cursor<MemoryTest> tests = r.find(ObjectFilters.eq("did",did), FindOptions.sort("timeEnded",SortOrder.Descending).thenLimit(offset, size));
        return tests.toList();
    }

    public double minBorderlineImpairedScore(int difficulty) {
        double borderlineImpairedScore = 0.0;
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        Cursor<MemoryTest> memoryTests = r.find(ObjectFilters.eq("difficulty",difficulty));
        double minScore = 0.0;
        double currentScore;
        for(MemoryTest memoryTest : memoryTests) {
            // Look for only training scores with a BAC and Borderline Impairment
            if(memoryTest.getBloodAlcoholContent() > 0 && memoryTest.getImpairment().equals(MemoryTest.Impairment.Borderline)) {
                currentScore = memoryTest.getScore();
                if(minScore == 0.0 || currentScore < minScore) minScore = currentScore;
            }
        }
        return minScore;
    }

    public double minImpairedScore(int difficulty) {
        double borderlineImpairedScore = 0.0;
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        Cursor<MemoryTest> memoryTests = r.find(ObjectFilters.eq("difficulty",difficulty));
        double minScore = 0.0;
        double currentScore;
        for(MemoryTest memoryTest : memoryTests) {
            // Look for only training scores with a BAC and Borderline Impairment
            if(memoryTest.getBloodAlcoholContent() > 0 && memoryTest.getImpairment().equals(MemoryTest.Impairment.Impaired)) {
                currentScore = memoryTest.getScore();
                if(minScore == 0.0 || currentScore < minScore) minScore = currentScore;
            }
        }
        return minScore;
    }

    public double minGrosslyImpairedScore(int difficulty) {
        double grosslyImpairedScore = 0.0;
        ObjectRepository<MemoryTest> r = dbMgr.getDb().getRepository(MemoryTest.class);
        Cursor<MemoryTest> memoryTests = r.find(ObjectFilters.eq("difficulty",difficulty));
        double minScore = 0.0;
        double currentScore;
        for(MemoryTest memoryTest : memoryTests) {
            // Look for only training scores with a BAC and Borderline Impairment
            if(memoryTest.getBloodAlcoholContent() > 0 && memoryTest.getImpairment().equals(MemoryTest.Impairment.Gross)) {
                currentScore = memoryTest.getScore();
                if(minScore == 0.0 || currentScore < minScore) minScore = currentScore;
            }
        }
        return minScore;
    }
}
