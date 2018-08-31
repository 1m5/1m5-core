package io.onemfive.core.did.dao;

import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.core.infovault.LocalFSDAO;
import io.onemfive.data.DID;
import io.onemfive.data.util.JSONParser;

import java.io.FileNotFoundException;

public class SaveDIDDAO extends LocalFSDAO {

    private DID didToSave;
    private Boolean autoCreate = true;

    public SaveDIDDAO(InfoVaultDB infoVaultDB, DID did, Boolean autoCreate) {
        super(infoVaultDB);
        this.didToSave = did;
        if(autoCreate != null) this.autoCreate = autoCreate;
    }

    @Override
    public void execute() {
        try {
            infoVaultDB.save(
                    JSONParser.toString(didToSave.toMap()).getBytes(),
                    didToSave.getAlias(),
                    autoCreate);
        } catch (FileNotFoundException e) {
            exception = e;
        }
    }
}
