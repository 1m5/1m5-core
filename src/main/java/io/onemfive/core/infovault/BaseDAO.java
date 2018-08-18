package io.onemfive.core.infovault;

public abstract class BaseDAO implements DAO {

    public static final int NO_ERROR = -1;

    protected Exception exception;
    protected int errorCode = NO_ERROR;

    protected InfoVaultDB infoVaultDB;

    public BaseDAO(InfoVaultDB infoVaultDB) {
        this.infoVaultDB = infoVaultDB;
    }

    public Exception getException() {
        return exception;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
