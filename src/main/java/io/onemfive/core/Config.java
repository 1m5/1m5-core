package io.onemfive.core;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Config {

    public static Properties loadFromClasspath(String name, Properties scProps) throws Exception {
        System.out.println("Loading properties file "+name+"...");
        Properties p = new Properties();
        if(scProps != null)
            p.putAll(scProps);
        InputStream is = null;
        try {
            is = Config.class.getClassLoader().getResourceAsStream(name);
            p.load(is);
            System.out.println("Loaded properties file "+name+" with following name-value pairs:");
            Enumeration propNames = p.propertyNames();
            while(propNames.hasMoreElements()){
                String propName = (String)propNames.nextElement();
                System.out.println(propName+":"+p.getProperty(propName));
            }
        } catch (Exception e) {
            System.out.println("Failed to load properties file "+name);
            throw e;
        } finally {
            if(is!=null)
                try { is.close();} catch (IOException e) {}
        }

        return p;
    }

    public static Properties loadFromBase(String name) throws IOException {
        System.out.println("Loading properties file "+name+"...");
        Properties p = new Properties();
        InputStream is = null;
        String path = OneMFiveAppContext.getInstance().getBaseDir()+"/"+name;
        System.out.println("Loading properties file from "+path+"...");
        File folder = new File(path);
        boolean pathExists = true;
        if(folder.exists()) {
            try {
                is = new FileInputStream(path);
                p.load(is);
                System.out.println("Loaded properties file " + path + " with following name-value pairs:");
                Enumeration propNames = p.propertyNames();
                while (propNames.hasMoreElements()) {
                    String propName = (String) propNames.nextElement();
                    System.out.println(propName + ":" + p.getProperty(propName));
                }
            } catch (Exception e) {
                System.out.println("Failed to load properties file " + path);
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
                System.out.println("Failed to create new file at: "+path);
                throw(e);
            }
        }
        if(!pathExists) {
            System.out.println("Couldn't create path: "+path);
        }

        return p;
    }

    public static void saveToClasspath(String name, Properties props) throws IOException {
        System.out.println("Saving properties file "+name+"...");
        props.store(new FileOutputStream(name), null);
    }

    public static void saveToBase(String name, Properties props) throws IOException {
        System.out.println("Saving properties file "+name+"...");
        String path = OneMFiveAppContext.getInstance().getBaseDir()+"/"+name;
        props.store(new FileOutputStream(path), null);
    }

}
