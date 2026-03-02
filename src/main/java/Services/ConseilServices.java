package Services;

import Entites.Conseil;
import IServices.IConseilServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConseilServices implements IConseilServices {

    private final Connection con;

    public ConseilServices() {
        con = Mydatabase.getInstance().getConnection();
        try {
            con.setCatalog("pidev");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // CRUD
    // =========================

    @Override
    public void ajouterConseil(Conseil c) {
        String sql = "INSERT INTO article_conseille (titre, contenu, categorie, Auteur, auteur_image, likes_count) " +
                "VALUES (?,?,?,?,?,0)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, safe(c.getTitre()));
            ps.setString(2, safe(c.getContenu()));
            ps.setString(3, safe(c.getCategorie()));
            ps.setString(4, safe(c.getAuteur()));
            ps.setString(5, safe(c.getAuteurImage()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur INSERT article_conseille: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifierConseil(Conseil c) {
        String sql = "UPDATE article_conseille SET titre=?, contenu=?, categorie=?, Auteur=?, auteur_image=? WHERE id_article=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, safe(c.getTitre()));
            ps.setString(2, safe(c.getContenu()));
            ps.setString(3, safe(c.getCategorie()));
            ps.setString(4, safe(c.getAuteur()));
            ps.setString(5, safe(c.getAuteurImage()));
            ps.setInt(6, c.getIdArticle());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur UPDATE article_conseille: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimerConseil(int idArticle) {
        String sql = "DELETE FROM article_conseille WHERE id_article=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idArticle);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur DELETE article_conseille: " + e.getMessage(), e);
        }
    }

    // =========================
    // READ (obligatoire car dans l'interface)
    // =========================

    @Override
    public List<Conseil> afficherConseils() {
        List<Conseil> list = new ArrayList<>();
        String sql = "SELECT id_article, titre, contenu, categorie, date_creation, Auteur, auteur_image, " +
                "IFNULL(likes_count,0) AS likes_count " +
                "FROM article_conseille " +
                "ORDER BY categorie ASC, likes_count DESC, date_creation DESC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur SELECT article_conseille: " + e.getMessage(), e);
        }

        return list;
    }

    @Override
    public Conseil getConseilById(int idArticle) {
        String sql = "SELECT id_article, titre, contenu, categorie, date_creation, Auteur, auteur_image, " +
                "IFNULL(likes_count,0) AS likes_count " +
                "FROM article_conseille WHERE id_article=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idArticle);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getConseilById: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public List<Conseil> rechercher(String keyword) {
        String k = (keyword == null) ? "" : keyword.trim();
        if (k.isEmpty()) return afficherConseils();

        List<Conseil> list = new ArrayList<>();
        String sql = "SELECT id_article, titre, contenu, categorie, date_creation, Auteur, auteur_image, " +
                "IFNULL(likes_count,0) AS likes_count " +
                "FROM article_conseille " +
                "WHERE LOWER(titre) LIKE ? OR LOWER(contenu) LIKE ? OR LOWER(Auteur) LIKE ? " +
                "ORDER BY categorie ASC, likes_count DESC, date_creation DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + k.toLowerCase() + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche article_conseille: " + e.getMessage(), e);
        }

        return list;
    }

    // =========================
    // LIKE SYSTEM
    // =========================

    @Override
    public int incrementLike(int idArticle) {
        String up = "UPDATE article_conseille SET likes_count = IFNULL(likes_count,0) + 1 WHERE id_article=?";
        try (PreparedStatement ps = con.prepareStatement(up)) {
            ps.setInt(1, idArticle);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur incrementLike: " + e.getMessage(), e);
        }
        return getLikesCount(idArticle);
    }

    public int decrementLike(int idArticle) {
        String upd = "UPDATE article_conseille " +
                "SET likes_count = GREATEST(IFNULL(likes_count,0) - 1, 0) " +
                "WHERE id_article=?";
        try (PreparedStatement ps = con.prepareStatement(upd)) {
            ps.setInt(1, idArticle);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur decrementLike: " + e.getMessage(), e);
        }
        return getLikesCount(idArticle);
    }

    public int getLikesCount(int idArticle) {
        String sql = "SELECT IFNULL(likes_count,0) AS likes_count FROM article_conseille WHERE id_article=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idArticle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("likes_count");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getLikesCount: " + e.getMessage(), e);
        }
        return 0;
    }

    // =========================
    // HELPERS
    // =========================

    private Conseil mapRow(ResultSet rs) throws SQLException {
        return new Conseil(
                rs.getInt("id_article"),
                rs.getString("titre"),
                rs.getString("contenu"),
                rs.getString("categorie"),
                rs.getTimestamp("date_creation"),
                rs.getString("Auteur"),
                rs.getString("auteur_image"),
                rs.getInt("likes_count")
        );
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}