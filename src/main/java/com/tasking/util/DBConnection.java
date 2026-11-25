package com.tasking.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralized database connection manager.
 *
 * Configuration is read, in order, from:
 * 1. Environment variables: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
 * 2. /db.properties on the classpath (WEB-INF/classes/db.properties)
 *
 * Never hardcode real credentials here. See db.properties.example.
 */
public final class DBConnection {

    private static final Properties CFG = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                CFG.load(in);
            }
        } catch (Exception e) {
            // Properties file is optional; environment variables can supply everything.
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL JDBC driver not found: " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    private static String cfg(String envKey, String propKey, String fallback) {
        String v = System.getenv(envKey);
        if (v == null || v.isBlank()) {
            v = CFG.getProperty(propKey);
        }
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }

    public static Connection getConnection() throws SQLException {
        String host = cfg("DB_HOST", "db.host", "localhost");
        String port = cfg("DB_PORT", "db.port", "5432"); // PostgreSQL default, fallback only
        String name = cfg("DB_NAME", "db.name", "tasking");
        String user = cfg("DB_USER", "db.user", null);
        String pass = cfg("DB_PASSWORD", "db.password", null);

        if (user == null) {
            throw new SQLException("Database user not configured. Set DB_USER environment variable "
                    + "or db.user in db.properties.");
        }

        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
        return DriverManager.getConnection(url, user, pass == null ? "" : pass);
    }
}
