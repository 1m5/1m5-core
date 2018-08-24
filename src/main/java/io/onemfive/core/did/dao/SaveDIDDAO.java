package io.onemfive.core.did.dao;

import io.onemfive.core.infovault.BaseDAO;
import io.onemfive.core.infovault.LocalFileSystemDB;
import io.onemfive.data.DID;
import io.onemfive.data.util.JSONParser;

import java.io.FileNotFoundException;

public class SaveDIDDAO extends BaseDAO {

    private DID didToSave;
    private Boolean autoCreate = true;

    public SaveDIDDAO(LocalFileSystemDB localFileSystemDB, DID did, Boolean autoCreate) {
        super(localFileSystemDB);
        this.didToSave = did;
        if(autoCreate != null) this.autoCreate = autoCreate;
    }

    @Override
    public void execute() {
        try {
            ((LocalFileSystemDB)infoVaultDB).save(
                    JSONParser.toString(didToSave.toMap()).getBytes(),
                    didToSave.getAlias(),
                    autoCreate);
        } catch (FileNotFoundException e) {
            exception = e;
        }
    }
}
