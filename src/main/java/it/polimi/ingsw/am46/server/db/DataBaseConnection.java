package it.polimi.ingsw.am46.server.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The type Data base connection.
 */
public class DataBaseConnection {

    private static String url;
    private static String user;
    private static String password;

    // Load credentials on first use: env vars DB_URL / DB_USER / DB_PASSWORD win,
    // otherwise fall back to db.properties (gitignored, see db.example.properties)
    static {
        Properties props = new Properties();
        // I try to look for where I saved the credentials (classLoader is the one that looks for the resource)
        try (InputStream input = DataBaseConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input != null) {
                props.load(input);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties: " + e.getMessage());
        }

        url      = setting("DB_URL", props.getProperty("db.url"));
        user     = setting("DB_USER", props.getProperty("db.user"));
        password = setting("DB_PASSWORD", props.getProperty("db.password"));

        if (url == null || user == null || password == null) {
            throw new RuntimeException("DB credentials missing: set DB_URL, DB_USER, DB_PASSWORD "
                    + "or create db.properties from db.example.properties");
        }
    }

    private static String setting(String envName, String fallback) {
        String value = System.getenv(envName);
        return value != null && !value.isBlank() ? value : fallback;
    }

    /**
     * Gets connection.
     *
     * @return the connection
     * @throws SQLException the sql exception
     */
    public static Connection getConnection() throws SQLException {
        System.out.println("[DB] Opening connection...");
        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println("[DB] Connection opened successfully");
        return conn;
    }
}