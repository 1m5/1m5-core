package io.onemfive.core.infovault.nitrite;

import io.onemfive.core.LifeCycle;
import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.data.Contract;
import io.onemfive.data.DID;
import io.onemfive.data.Persistable;
import org.dizitart.no2.*;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 * TODO: Add Encrypt/Decrypt at Document level
 * @author objectorange
 */
public class NitriteDBManager implements LifeCycle {

    private static final Logger LOG = Logger.getLogger(NitriteDBManager.class.getName());

    private Nitrite db = null;

    private static String dbFolder = "/ndb/";
    private static String dbName = "info.db";
    private static String dbFullPath;
    // TODO: Externalize username and passwords with user supplied
    private static String dbUsername = "1M5";
    private static String dbUserPassword = "h!zeUB2k8jgbMdPas";
    private static String encryptPassword = "fNoaizM!5rsKt726newjxYpU3";
    private static String encryptPasswordCipher = "AES-256";

    private NitriteCollection idCollection;

    public Nitrite getDb() {
        return db;
    }

    synchronized long nextId(String name) {
        Document d = idCollection.find().firstOrDefault();
        Long nextId = (Long) d.get(name);
        if (nextId == null) {
            nextId = 1L;
        }
        d.put(name, nextId + 1);
        idCollection.update(d);
        return nextId;
    }

    public boolean saveObject(Persistable persistable) {
        if(persistable instanceof Contract) {
            Contract c = (Contract)persistable;
            if(c.getId() == null) {
                db.getRepository(Contract.class).insert(c);
            } else {
                db.getRepository(Contract.class).update(c);
            }
        } else {
            LOG.warning(NitriteDBManager.class.getName()+": No support for saving class="+persistable.getClass().getName());
        }
        return true;
    }

    public Persistable loadObject(Persistable persistable) {
        if(persistable instanceof Contract) {
            Contract c = (Contract)persistable;
            if(c.getId() != null) {
                return db.getRepository(Contract.class).getById(NitriteId.createId(c.getId()));
            } else {
                LOG.warning("No support for finding objects during load.");
            }
        } else {
            LOG.warning("No support for saving class="+persistable.getClass().getName());
        }
        return null;
    }

    public List<Persistable> loadObjectList(Class clazz, Integer offset, Integer pageSize, String sortBy, Boolean ascending) {
        List<Persistable> persistables = new ArrayList<>();
        if(clazz.getName().equals(Contract.class.getName())) {
            ObjectRepository<Contract> repository = db.getRepository(Contract.class);
            Cursor cursor = repository.find();
            for(Object obj : cursor) {
                persistables.add((Persistable)obj);
            }
        } else {
            LOG.warning("No support for saving class="+clazz.getName());
        }
        return persistables;
    }

    public boolean saveCollection(String name, Map<String,Object> data) {
        // External entity -> use collection
        NitriteCollection c = db.getCollection(name);
        Document d = new Document(data);
        if (data.get("_id") == null) {
            c.insert(d);
        } else {
            c.update(d);
        }
        c.close();
        return true;
    }

    public Map<String,Object> loadCollection(String name, Long id, DID did) {
        NitriteCollection c = db.getCollection(name);
        Document d = c.getById(NitriteId.createId(id));
        c.close();
        return d;
    }

    public List<Map<String,Object>> loadCollectionList(String name, Integer offset, Integer pageSize, String sortBy, Boolean ascending) {
        NitriteCollection c = db.getCollection(name);
        List<Document> documents;
        if(offset == null) offset = 0;
        if(pageSize == null) pageSize = 10;

        FindOptions sortOptions = null;
        if(sortBy != null) {
            sortOptions = FindOptions.sort(sortBy, ascending ? SortOrder.Ascending : SortOrder.Descending);
        }

        if(sortOptions == null) {
            documents = c.find(FindOptions.limit(offset, pageSize)).toList();
        } else {
            documents = c.find(sortOptions.thenLimit(offset, pageSize)).toList();
        }

        List<Map<String,Object>> results = new ArrayList<>((int)c.size());
        for(Document d : documents) {
            results.add(d);
        }
        return results;
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        dbFullPath = OneMFiveAppContext.getInstance().getBaseDir()+dbFolder;
        File f = new File(dbFullPath);
        if(!f.exists())
            if(!f.mkdir()) {
                LOG.severe("Unable to create base directory for NitriteDB: "+dbFullPath+"; exiting...");
                return false;
            }
        db = Nitrite.builder()
                .compressed()
                .filePath(dbFullPath+dbName)
                .openOrCreate(dbUsername, dbUserPassword);
//        db.getCollection("id").drop();
        idCollection = db.getCollection("id");
        // ensure primed
        org.dizitart.no2.Cursor c = idCollection.find();
        if(c.size() == 0) {
            idCollection.insert(new Document());
        }
        // observe any change to the collection
//        idCollection.register(new ChangeListener() {
//
//            @Override
//            public void onChange(ChangeInfo changeInfo) {
//
//            }
//        });
        LOG.info("Started.");
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
        LOG.info("Shutting down...");
        if(db != null && !db.isClosed()) {
            db.close();
        }
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
