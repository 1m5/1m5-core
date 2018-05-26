package io.onemfive.core;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Config {

    private static final Logger LOG = Logger.getLogger(Config.class.getName());

    public static Properties loadFromClasspath(String name, Properties scProps) throws Exception {
        LOG.info("Loading properties file "+name+"...");
        Properties p = new Properties();
        if(scProps != null)
            p.putAll(scProps);
        InputStream is = null;
        try {
            is = Config.class.getClassLoader().getResourceAsStream(name);
            p.load(is);
            LOG.info("Loaded properties file "+name+" with following name-value pairs:");
            Enumeration propNames = p.propertyNames();
            while(propNames.hasMoreElements()){
                String propName = (String)propNames.nextElement();
                LOG.info(propName+":"+p.getProperty(propName));
            }
        } catch (Exception e) {
            LOG.warning("Failed to load properties file "+name);
            throw e;
        } finally {
            if(is!=null)
                try { is.close();} catch (IOException e) {}
        }

        return p;
    }

    public static Properties loadFromBase(String name) throws IOException {
        LOG.info("Loading properties file "+name+"...");
        Properties p = new Properties();
        InputStream is = null;
        String path = OneMFiveAppContext.getInstance().getBaseDir()+"/"+name;
        LOG.info("Loading properties file from "+path+"...");
        File folder = new File(path);
        boolean pathExists = true;
        if(folder.exists()) {
            try {
                is = new FileInputStream(path);
                p.load(is);
                LOG.info("Loaded properties file " + path + " with following name-value pairs:");
                Enumeration propNames = p.propertyNames();
                while (propNames.hasMoreElements()) {
                    String propName = (String) propNames.nextElement();
                    LOG.info(propName + ":" + p.getProperty(propName));
                }
            } catch (Exception e) {
                LOG.warning("Failed to load properties file " + path);
                throw e;
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
            }
        } else {
            try {
                pathExists = folder.createNewFile();
            } catch (IOException e) {
                LOG.warning("Failed to create new file at: "+path);
                throw(e);
            }
        }
        if(!pathExists) {
            LOG.warning("Couldn't create path: "+path);
        }

        return p;
    }

    public static void saveToClasspath(String name, Properties props) throws IOException {
        LOG.info("Saving properties file "+name+"...");
        props.store(new FileOutputStream(name), null);
    }

    public static void saveToBase(String name, Properties props) throws IOException {
        LOG.info("Saving properties file "+name+"...");
        String path = OneMFiveAppContext.getInstance().getBaseDir()+"/"+name;
        props.store(new FileOutputStream(path), null);
    }

}
