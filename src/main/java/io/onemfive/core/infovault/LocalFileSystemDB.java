package io.onemfive.core.infovault;

import io.onemfive.core.OneMFiveAppContext;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class LocalFileSystemDB implements InfoVaultDB {

    private Logger LOG = Logger.getLogger(LocalFileSystemDB.class.getName());

    private File dbDir;
    private Status status = Status.Shutdown;

    @Override
    public void execute(DAO dao) throws Exception {
        dao.execute();
    }

    public void save(byte[] content, String fileName, boolean autoCreate) throws FileNotFoundException {
        File file = new File(dbDir, fileName);
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

    public byte[] load(String fileName) throws FileNotFoundException {
        File file = new File(dbDir, fileName);
        byte[] buffer = new byte[8 * 1024];
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int b;
            while((b = in.read(buffer)) != -1) {
                out.write(buffer, 0, b);
            }
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
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean init(Properties properties) {
        status = Status.Starting;
        File baseDir = OneMFiveAppContext.getInstance().getBaseDir();
        dbDir = new File(baseDir, "/infovault");
        if(!dbDir.exists()) {
            if(!dbDir.mkdir()) {
                LOG.warning("Unable to create directory /infovault in 1M5 base directory.");
                status = Status.StartupFailed;
                return false;
            }
        }
        status = Status.Running;
        return true;
    }

    @Override
    public boolean teardown() {
        status = Status.Stopping;

        status = Status.Shutdown;
        return true;
    }

//    public static void main(String[] args) {
//        DID did = new DID();
//        did.setAlias("Alice");
//        did.setIdentityHash(HashUtil.generateHash(did.getAlias()));
//
//        LocalFileSystemDB s = new LocalFileSystemDB();
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
