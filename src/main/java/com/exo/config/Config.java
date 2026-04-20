package com.exo.config;

import java.io.InputStream;
import java.util.Properties;

public final class Config {
    private static final Properties P = new Properties();

    static {
        try (InputStream in = Config.class
                .getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new RuntimeException("db.properties not found!");
            P.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config: " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        return P.getProperty(key);
    }
}