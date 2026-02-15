package tn.esprit.pidev;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NiveauJeuService {

    public List<NiveauJeu> getAll() {
        List<NiveauJeu> list = new ArrayList<>();
        String sql = """
                SELECT id, libelle, min_moyenne, max_moyenne, description
                FROM niveau_jeu
                ORDER BY id
                """;

        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return list;

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new NiveauJeu(
                        rs.getInt("id"),
                        rs.getString("libelle"),
                        rs.getDouble("min_moyenne"),
                        rs.getDouble("max_moyenne"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll NiveauJeu: " + e.getMessage());
        }
        return list;
    }

    public List<NiveauJeu> search(String keyword) {
        List<NiveauJeu> list = new ArrayList<>();
        String k = (keyword == null) ? "" : keyword.trim();

        String sql = """
                SELECT id, libelle, min_moyenne, max_moyenne, description
                FROM niveau_jeu
                WHERE CAST(id AS CHAR) LIKE ?
                   OR libelle LIKE ?
                ORDER BY id
                """;

        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return list;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String like = "%" + k + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new NiveauJeu(
                            rs.getInt("id"),
                            rs.getString("libelle"),
                            rs.getDouble("min_moyenne"),
                            rs.getDouble("max_moyenne"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur search NiveauJeu: " + e.getMessage());
        }
        return list;
    }

    public boolean add(NiveauJeu n) {
        String sql = "INSERT INTO niveau_jeu(libelle, min_moyenne, max_moyenne, description) VALUES(?,?,?,?)";
        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return false;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, n.getLibelle());
            ps.setDouble(2, n.getMinMoyenne());
            ps.setDouble(3, n.getMaxMoyenne());
            ps.setString(4, n.getDescription());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur add NiveauJeu: " + e.getMessage());
            return false;
        }
    }

    public boolean update(NiveauJeu n) {
        String sql = "UPDATE niveau_jeu SET libelle=?, min_moyenne=?, max_moyenne=?, description=? WHERE id=?";
        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return false;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, n.getLibelle());
            ps.setDouble(2, n.getMinMoyenne());
            ps.setDouble(3, n.getMaxMoyenne());
            ps.setString(4, n.getDescription());
            ps.setInt(5, n.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur update NiveauJeu: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM niveau_jeu WHERE id=?";
        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return false;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur delete NiveauJeu: " + e.getMessage());
            return false;
        }
    }
}
