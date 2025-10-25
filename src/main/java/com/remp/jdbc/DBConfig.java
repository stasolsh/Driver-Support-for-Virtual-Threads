package com.remp.jdbc;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class DBConfig {
    private final static Logger LOGGER = Logger.getLogger("com.remp.jdbc.DBConfig");
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                System.exit(1);
            }
            // load a properties file from class path, inside static method
            CONFIG.load(input);
        } catch (Exception e) {
            LOGGER.severe(String.format("Exception during config load: %s with cause: %s", e.getMessage(), e.getCause()));
        }
    }

    private static final String DB_USER = CONFIG.getProperty("DB_USER");

    private static final String DB_URL = CONFIG.getProperty("DB_URL");

    private static final String DB_PASSWORD = CONFIG.getProperty("DB_PASSWORD");

    public static String getDbUser() {
        return DB_USER;
    }

    public static String getDbUrl() {
        return DB_URL;
    }

    public static String getDbPassword() {
        return DB_PASSWORD;
    }
}
