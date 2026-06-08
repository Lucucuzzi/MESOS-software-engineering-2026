package it.polimi.ingsw.am46.server.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataBaseConnection {

    private static String url;
    private static String user;
    private static String password;

    // Carica le credenziali dal file properties al primo utilizzo
    static {
        // provo a cercare dove ho salvato le credenziali (classLoader è quello che va a cercare la risorsa)
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

    public static Connection getConnection() throws SQLException {
        System.out.println("[DB] Apertura connessione...");
        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println("[DB] Connessione aperta con successo");
        return conn;
    }
}