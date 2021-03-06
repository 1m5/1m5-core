package io.onemfive.core.infovault;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class LocalFSInfoVaultDB implements InfoVaultDB {

    private Logger LOG = Logger.getLogger(LocalFSInfoVaultDB.class.getName());

    private File dbDir;
    private Status status = Status.Shutdown;

    public LocalFSInfoVaultDB() {}

    @Override
    public void execute(DAO dao) throws Exception {

    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean teardown() {

        return true;
    }

    public void save(String label, String key, byte[] content, boolean autoCreate) throws FileNotFoundException {
        LOG.info("Saving content...");
        File path = null;
        if(label != null) {
            path = new File(dbDir, label);
            if(!path.exists()) {
                if(!autoCreate)
                    throw new FileNotFoundException("Label doesn't exist and autoCreate = false");
                else {
                    path.mkdirs();
                    path.setWritable(true);
                }
            }
        }
        File file = null;
        if(path == null)
            file = new File(dbDir, key);
        else
            file = new File(path, key);
        file.setWritable(true);

        if(!file.exists() && autoCreate) {
            try {
                if(!file.createNewFile()) {
                    LOG.warning("Unable to create new file.");
                    return;
                }
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
                return;
            }
        }
        byte[] buffer = new byte[8 * 1024];
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        FileOutputStream out = new FileOutputStream(file);
        try {
            int b;
            while ((b = in.read(buffer)) != -1) {
                out.write(buffer, 0, b);
            }
            LOG.info("Content saved.");
        } catch (IOException ex) {
            LOG.warning(ex.getLocalizedMessage());
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
    }

    public byte[] load(String label, String key) throws FileNotFoundException {
        LOG.info("Loading content for label: "+label+" and key: "+key);
        File path = null;
        if(label != null) {
            path = new File(dbDir, label);
            if(!path.exists()) {
                throw new FileNotFoundException("Label doesn't exist");
            }
        }

        File file = null;
        if(path == null)
            file = new File(dbDir, key);
        else
            file = new File(path, key);

        return loadFile(file);
    }

    @Override
    public List<byte[]> loadAll(String label) {
        LOG.info("Loading all content for label: "+label);
        List<byte[]> contentList = new ArrayList<>();
        File path = null;
        if(label != null) {
            path = new File(dbDir, label);
            if(path.exists()) {
                File[] children = path.listFiles();
                for(File f : children) {
                    try {
                        contentList.add(loadFile(f));
                    } catch (FileNotFoundException e) {
                        LOG.warning("File not found: "+f.getAbsolutePath());
                    }
                }
            }
        }
        return contentList;
    }

    private byte[] loadFile(File file) throws FileNotFoundException {
        byte[] buffer = new byte[8 * 1024];
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int b;
            while((b = in.read(buffer)) != -1) {
                out.write(buffer, 0, b);
            }
            LOG.info("Content loaded.");
        } catch (IOException ex) {
            LOG.warning(ex.getLocalizedMessage());
            return null;
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException ex) {
                LOG.warning(ex.getLocalizedMessage());
            }
        }
        return out.toByteArray();
    }

    @Override
    public boolean init(Properties properties) {
        File baseDir = new File(properties.getProperty("1m5.dir.services.io.onemfive.core.infovault.InfoVaultService"));
        if(!baseDir.exists() && !baseDir.mkdir()) {
            LOG.warning("Unable to build InfoVaultService directory at: "+baseDir.getAbsolutePath());
            return false;
        } else {
            baseDir.setWritable(true);
        }
        dbDir = new File(baseDir, this.getClass().getSimpleName());
        if(!dbDir.exists() && !dbDir.mkdir()) {
            LOG.warning("Unable to create directory for LocalFSInfoVaultDB.");
            return false;
        } else {
            dbDir.setWritable(true);
        }
        return true;
    }

//    public static void main(String[] args) {
//        DID did = new DID();
//        did.setAlias("Alice");
//        did.setIdentityHash(HashUtil.generateHash(did.getAlias()));
//
//        LocalFSInfoVaultDB s = new LocalFSInfoVaultDB();
//        s.dbDir = new File("dbDir");
//        if(!s.dbDir.exists()) {
//            if (!s.dbDir.mkdir()) {
//                System.out.println("Unable to make dbDir.");
//                return;
//            }
//        }
//
//        SaveDIDDAO saveDIDDAO = new SaveDIDDAO(s, did, true);
//        saveDIDDAO.execute();
//
//        DID did2 = new DID();
//        did2.setAlias("Alice");
//
//        LoadDIDDAO loadDIDDAO = new LoadDIDDAO(s, did2);
//        loadDIDDAO.execute();
//        DID didLoaded = loadDIDDAO.getLoadedDID();
//
//        System.out.println("did1.hash: "+did.getIdentityHash());
//        System.out.println("did2.hash: "+didLoaded.getIdentityHash());
//    }
}
