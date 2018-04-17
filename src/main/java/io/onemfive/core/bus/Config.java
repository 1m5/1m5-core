package io.onemfive.core.bus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Config {

    public static Properties load(String name, Properties scProps) throws Exception {
        System.out.println("Loading properties file "+name+"...");
        Properties p = new Properties();
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

}
