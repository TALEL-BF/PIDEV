package tn.esprit.pidev;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuiviTotalService {

    public List<SuiviTotal> getAll() {
        List<SuiviTotal> list = new ArrayList<>();

        String sql = """
            SELECT s.id, s.enfant_id, s.note_evaluation, s.note_consultation,
                   s.moyenne, s.niveau_id, n.libelle AS niveau_libelle
            FROM suivi_total s
            LEFT JOIN niveau_jeu n ON n.id = s.niveau_id
            ORDER BY s.id
        """;

        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return list;

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Integer nivId = rs.getObject("niveau_id") == null ? null : rs.getInt("niveau_id");

                list.add(new SuiviTotal(
                        rs.getInt("id"),
                        rs.getInt("enfant_id"),
                        rs.getDouble("note_evaluation"),
                        rs.getDouble("note_consultation"),
                        rs.getDouble("moyenne"),
                        nivId,
                        rs.getString("niveau_libelle")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll SuiviTotal: " + e.getMessage());
        }

        return list;
    }

    public List<SuiviTotal> search(String keyword) {
        List<SuiviTotal> list = new ArrayList<>();
        String k = (keyword == null) ? "" : keyword.trim();

        String sql = """
            SELECT s.id, s.enfant_id, s.note_evaluation, s.note_consultation,
                   s.moyenne, s.niveau_id, n.libelle AS niveau_libelle
            FROM suivi_total s
            LEFT JOIN niveau_jeu n ON n.id = s.niveau_id
            WHERE CAST(s.id AS CHAR) LIKE ?
               OR CAST(s.enfant_id AS CHAR) LIKE ?
               OR n.libelle LIKE ?
            ORDER BY s.id
        """;

        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return list;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String like = "%" + k + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer nivId = rs.getObject("niveau_id") == null ? null : rs.getInt("niveau_id");

                    list.add(new SuiviTotal(
                            rs.getInt("id"),
                            rs.getInt("enfant_id"),
                            rs.getDouble("note_evaluation"),
                            rs.getDouble("note_consultation"),
                            rs.getDouble("moyenne"),
                            nivId,
                            rs.getString("niveau_libelle")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur search SuiviTotal: " + e.getMessage());
        }

        return list;
    }

    public boolean add(int enfantId, double noteEval, double noteConsult) {
        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return false;

        double moyenne = (noteEval + noteConsult) / 2.0;
        Integer niveauId = findNiveauIdByMoyenne(cnx, moyenne);

        if (niveauId == null) {
            System.out.println("Erreur add SuiviTotal: aucun niveau trouvé pour moyenne=" + moyenne);
            return false;
        }

        String sql = """
            INSERT INTO suivi_total(enfant_id, note_evaluation, note_consultation, moyenne, niveau_id)
            VALUES(?,?,?,?,?)
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, enfantId);
            ps.setDouble(2, noteEval);
            ps.setDouble(3, noteConsult);
            ps.setDouble(4, moyenne);
            ps.setInt(5, niveauId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Erreur add SuiviTotal: " + e.getMessage());
            return false;
        }
    }

    public boolean update(int id, int enfantId, double noteEval, double noteConsult) {
        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return false;

        double moyenne = (noteEval + noteConsult) / 2.0;
        Integer niveauId = findNiveauIdByMoyenne(cnx, moyenne);

        if (niveauId == null) {
            System.out.println("Erreur update SuiviTotal: aucun niveau trouvé pour moyenne=" + moyenne);
            return false;
        }

        String sql = """
            UPDATE suivi_total
            SET enfant_id=?, note_evaluation=?, note_consultation=?, moyenne=?, niveau_id=?
            WHERE id=?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, enfantId);
            ps.setDouble(2, noteEval);
            ps.setDouble(3, noteConsult);
            ps.setDouble(4, moyenne);
            ps.setInt(5, niveauId);
            ps.setInt(6, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur update SuiviTotal: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM suivi_total WHERE id=?";
        Connection cnx = MyConnection.getInstance();
        if (cnx == null) return false;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur delete SuiviTotal: " + e.getMessage());
            return false;
        }
    }

    private Integer findNiveauIdByMoyenne(Connection cnx, double moyenne) {
        String sql = """
            SELECT id
            FROM niveau_jeu
            WHERE ? >= min_moyenne AND ? <= max_moyenne
            ORDER BY id
            LIMIT 1
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, moyenne);
            ps.setDouble(2, moyenne);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.out.println("Erreur findNiveauIdByMoyenne: " + e.getMessage());
        }

        return null;
    }
}
