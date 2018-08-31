package io.onemfive.core.infovault;

import java.io.Serializable;

public interface DAO extends Serializable {
    void execute() throws Exception;
}
