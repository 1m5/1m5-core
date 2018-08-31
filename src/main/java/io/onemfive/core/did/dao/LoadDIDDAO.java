package io.onemfive.core.did.dao;

import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.core.infovault.LocalFSDAO;
import io.onemfive.data.DID;
import io.onemfive.data.util.JSONParser;

import java.io.FileNotFoundException;
import java.util.Map;

public class LoadDIDDAO extends LocalFSDAO {

    private DID providedDID;
    private DID loadedDID;

    public LoadDIDDAO(InfoVaultDB infoVaultDB, DID did) {
        super(infoVaultDB);
        this.providedDID = did;
    }

    @Override
    public void execute() {
        byte[] content;
        try {
            content = infoVaultDB.load(providedDID.getAlias());
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
