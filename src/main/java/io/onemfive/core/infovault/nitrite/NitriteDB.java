package io.onemfive.core.infovault.nitrite;

import io.onemfive.core.LifeCycle;
import io.onemfive.core.OneMFiveAppContext;
import org.dizitart.no2.*;
import org.dizitart.no2.filters.Filters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: Add Description
 * TODO: Add Encrypt/Decrypt at Document level
 * @author objectorange
 */
public class NitriteDB implements LifeCycle {

    private Nitrite db = null;

    private static String dbFolder = "/data/";
    private static String dbName = "info.db";
    private static String dbFullPath;
    // TODO: Externalize username and passwords with user supplied
    private static String dbUsername = "1M5";
    private static String dbUserPassword = "h!zeUB2k8jgbMdPas";
    private static String encryptPassword = "fNoaizM!5rsKt726newjxYpU3";
    private static String encryptPasswordCipher = "AES-256";

    public boolean save(String name, Map<String,Object> data) {
        NitriteCollection c = db.getCollection(name);
        Document d = new Document(data);
        if(data.get("_id") == null) {
            c.insert(d);
        } else {
            c.update(d);
        }
        c.close();
        return true;
    }

    public Map<String,Object> load(String name, Long id) {
        NitriteCollection c = db.getCollection(name);
        Document d = c.getById(NitriteId.createId(id));
        c.close();
        return d;
    }

    public List<Map<String,Object>> loadCollection(String name, Integer offset, Integer pageSize, String sortBy, Boolean ascending) {
        NitriteCollection c = db.getCollection(name);
        Cursor documents;
        if(offset == null) offset = 0;
        if(pageSize == null) pageSize = 10;

        FindOptions sortOptions = null;
        if(sortBy != null) {
            sortOptions = FindOptions.sort(sortBy, ascending ? SortOrder.Ascending : SortOrder.Descending);
        }

        if(sortOptions == null) {
            documents = c.find(FindOptions.limit(offset, pageSize));
        } else {
            documents = c.find(sortOptions.thenLimit(offset, pageSize));
        }

        List<Map<String,Object>> results = new ArrayList<>((int)c.size());
        for(Document d : documents) {
            results.add(d);
        }
        return results;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("NitriteDB starting...");
        dbFullPath = OneMFiveAppContext.getInstance().getBaseDir()+dbFolder;
        File f = new File(dbFullPath);
        if(!f.exists()) f.mkdir();
        db = Nitrite.builder()
                .compressed()
                .filePath(dbFullPath+dbName)
                .openOrCreate(dbUsername, dbUserPassword);
        System.out.println("NitriteDB started.");
        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        System.out.println("NitriteDB shutting down...");
        if(db != null && !db.isClosed()) {
            db.close();
        }
        System.out.println("NitriteDB shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
