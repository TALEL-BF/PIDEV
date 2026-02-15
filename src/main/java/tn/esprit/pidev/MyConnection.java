package tn.esprit.pidev;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MyConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/pidev_db"
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true"
                    + "&useUnicode=true"
                    + "&characterEncoding=UTF-8";

    private static final String USER = "pidev";
    private static final String PASSWORD = "pidev123";

    private static Connection connection;

    private MyConnection() {}

    public static Connection getInstance() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion réussie à la base !");
            }
        } catch (SQLException e) {
            connection = null;
            System.out.println("Erreur connexion MySQL: " + e.getMessage());
        }
        return connection;
    }

    public static void close() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
            connection = null;
        }
    }
}
