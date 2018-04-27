package io.onemfive.core.infovault.h2;

import io.onemfive.core.LifeCycle;
import io.onemfive.core.OneMFiveAppContext;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class H2DB implements LifeCycle {

    // TODO: Support database on internal flash drive
    // TODO: Support database on external flash drive
    private static String dbFolder = "/data/";
    private static String dbName = "info";
    private static String dbCipher = "AES";
    private static String dbUsername = "1M5";
    private static String dbUserPassword = "h!zeUB2k8jgbMdPas";
    // TODO: Replace filepassword with user supplied
    private static String dbFilePassword = "fNoaizM!5rsKt726newjxYpU3";

    private String dbConnUrl;
    private Connection conn = null;

    private Connection buildConnection() {
        if(conn == null) {
            try {
                conn = DriverManager.getConnection(dbConnUrl, dbUsername, dbFilePassword + " " + dbUserPassword);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    @Override
    public boolean start(Properties properties) {
        String dbFullPath = OneMFiveAppContext.getInstance().getBaseDir()+dbFolder;
        File f = new File(dbFullPath);
        if(!f.exists()) f.mkdir();
        dbConnUrl = "jdbc:h2:file:"+dbFullPath+dbName+";CIPHER="+dbCipher;
        try {
            // ensure on startup we can connect
            conn = DriverManager.getConnection(dbConnUrl, dbUsername, dbFilePassword + " " + dbUserPassword);
        } catch (SQLException e) {
            System.out.println("InfoVaultService failed to started: "+e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
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
        try {
            if(conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn = null;
        }
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
