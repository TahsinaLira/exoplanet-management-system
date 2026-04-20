// com.exo.config.Db
package com.exo.config;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Db {
    private static final Properties P = new Properties();
    private static volatile boolean loaded = false;

    private static void load() {
        if (loaded) return;
        synchronized (Db.class) {
            if (loaded) return;
            try (InputStream in = Db.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) P.load(in); // don't throw on missing
            } catch (Exception ignore) {}
            loaded = true;
        }
    }

    public static Connection getConnection() throws SQLException {
        load();
        String url  = P.getProperty("db.url");
        String user = P.getProperty("db.user");
        String pass = P.getProperty("db.pass");
        if (url == null || user == null) throw new SQLException("DB config missing");
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException ign) {}
        DriverManager.setLoginTimeout(5);
        return DriverManager.getConnection(url, user, pass);
    }
}