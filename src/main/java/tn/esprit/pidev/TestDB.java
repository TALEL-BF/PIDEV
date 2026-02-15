package tn.esprit.pidev;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance();

        String sql = "SELECT id, libelle, min_moyenne, max_moyenne FROM niveau_jeu";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + " | " +
                                rs.getString("libelle") + " | " +
                                rs.getDouble("min_moyenne") + " -> " +
                                rs.getDouble("max_moyenne")
                );
            }

        } catch (Exception e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }
}
