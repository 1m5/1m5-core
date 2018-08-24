package io.onemfive.core.did.dao;

import io.onemfive.core.infovault.BaseDAO;
import io.onemfive.core.infovault.LocalFileSystemDB;
import io.onemfive.data.DID;
import io.onemfive.data.util.JSONParser;

import java.io.FileNotFoundException;
import java.util.Map;

public class LoadDIDDAO extends BaseDAO {

    private DID providedDID;
    private DID loadedDID;

    public LoadDIDDAO(LocalFileSystemDB localFileSystemDB, DID did) {
        super(localFileSystemDB);
        this.providedDID = did;
    }

    @Override
    public void execute() {
        byte[] content;
        try {
            content = ((LocalFileSystemDB)infoVaultDB).load(providedDID.getAlias());
        } catch (FileNotFoundException e) {
            exception = e;
            return;
        }
        String jsonBody = new String(content);
        loadedDID = new DID();
        loadedDID.fromMap((Map<String,Object>)JSONParser.parse(jsonBody));
    }

    public DID getLoadedDID() {
        return loadedDID;
    }
}
