package io.onemfive.core.infovault;

import io.onemfive.core.OneMFiveAppContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class LocalFSInfoVaultDB implements InfoVaultDB {

    private Logger LOG = Logger.getLogger(LocalFSInfoVaultDB.class.getName());

    private File dbDir;
    private Status status = Status.Shutdown;

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
                else
                    path.mkdirs();
            }
        }
        File file = null;
        if(path == null)
            file = new File(dbDir, key);
        else
            file = new File(path, key);

        if(!file.exists() && autoCreate) {
            if(!file.canWrite()) {
                LOG.warning("No write access for directory: "+dbDir.getAbsolutePath());
                return;
            }
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
        File baseDir = OneMFiveAppContext.getInstance().getBaseDir();
        if(!baseDir.exists()) {
            LOG.warning("Base directory for 1M5 does not exist.");
            return false;
        }
        dbDir = new File(baseDir, "/infovault");
        if(!dbDir.exists()) {
            if(!dbDir.mkdir()) {
                LOG.warning("Unable to create directory /infovault in 1M5 base directory.");
                return false;
            }
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