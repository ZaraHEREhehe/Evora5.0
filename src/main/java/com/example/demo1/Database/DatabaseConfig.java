package com.example.demo1.Database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new RuntimeException("Unable to find database.properties. " +
                        "Please create it from database.properties.template");
            }

            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUsername() {
        return properties.getProperty("db.username");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }

    public static String getDriver() {
        return properties.getProperty("db.driver");
    }

    public static int getInitialPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.initialSize", "5"));
    }

    public static int getMaxTotal() {
        return Integer.parseInt(properties.getProperty("db.pool.maxTotal", "20"));
    }
}