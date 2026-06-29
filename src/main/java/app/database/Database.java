package app.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database utility class for managing DB connection and initialization.
 * Uses SQLite for simplicity and jdbc for DB driver.
 */
public class Database {

    // relative path (dev/submission, run from project root)
    private static final String URL = "jdbc:sqlite:database/sharespace.db";

    private static Connection connection;

    /**
     * Initialize method to set up the database from schema.sql and seed.sql.
     */
    public static void initialize() {
        Connection conn = getConnection();
        executeScript(conn, "/schema.sql");
        executeScript(conn, "/seed.sql");
    }

    /**
     * getConnection method to provide a singleton DB connection.
     */
    public static Connection getConnection() {
        if (connection == null) {
            // ensure the database/ directory exists before SQLite tries to create the file
            new java.io.File("database").mkdirs();
            try {
                connection = DriverManager.getConnection(URL);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            } catch (SQLException e) {
                throw new RuntimeException("Database connection failed", e);
            }
        }
        return connection;
    }

    private static void executeScript(Connection conn, String resourcePath) {
        try (InputStream is = Database.class.getResourceAsStream(resourcePath)) {
            if (is == null) { 
                throw new RuntimeException(resourcePath + " not found in classpath");
            }

            String sql = new String(is.readAllBytes());
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    conn.createStatement().execute(trimmed);
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to execute " + resourcePath, e);
        }
    }
}
