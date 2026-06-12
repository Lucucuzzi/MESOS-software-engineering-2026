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

    // Load credentials from properties file on first use
    static {
        // I try to look for where I saved the credentials (classLoader is the one that looks for the resource)
        try (InputStream input = DataBaseConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("db.properties not found. ");
            }

            Properties props = new Properties();
            props.load(input);

            url      = props.getProperty("db.url");
            user     = props.getProperty("db.user");
            password = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties: " + e.getMessage());
        }
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