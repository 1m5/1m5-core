package io.onemfive.core.infovault;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

/**
 * InfoVaultDB
 *
 * Stores personal information securely while allowing access
 * by other parties with personal approval.
 *
 * @author objectorange
 */
public interface InfoVaultDB {

    enum Status {Starting,StartupFailed,Running,Stopping,Shutdown}

    void execute(DAO dao) throws Exception;

    void save(String label, String key, byte[] content, boolean autoCreate) throws FileNotFoundException;

    byte[] load(String label, String key) throws FileNotFoundException;

    List<byte[]> loadAll(String label);

    Status getStatus();

    boolean init(Properties properties);

    boolean teardown();

}
