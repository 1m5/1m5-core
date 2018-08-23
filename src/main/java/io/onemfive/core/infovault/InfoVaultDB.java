package io.onemfive.core.infovault;

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

    Status getStatus();

    boolean init(Properties properties);

    boolean teardown();

}
