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

    @Override
    public void ajouterConseil(Conseil c) {
        String sql = "INSERT INTO article_conseille (titre, contenu, categorie, Auteur) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, safe(c.getTitre()));
            ps.setString(2, safe(c.getContenu()));
            ps.setString(3, safe(c.getCategorie()));
            ps.setString(4, safe(c.getAuteur()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur INSERT article_conseille: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifierConseil(Conseil c) {
        String sql = "UPDATE article_conseille SET titre=?, contenu=?, categorie=?, Auteur=? WHERE id_article=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, safe(c.getTitre()));
            ps.setString(2, safe(c.getContenu()));
            ps.setString(3, safe(c.getCategorie()));
            ps.setString(4, safe(c.getAuteur()));
            ps.setInt(5, c.getIdArticle());
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

    @Override
    public List<Conseil> afficherConseils() {
        List<Conseil> list = new ArrayList<>();
        String sql = "SELECT id_article, titre, contenu, categorie, date_creation, Auteur " +
                "FROM article_conseille ORDER BY date_creation DESC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Conseil c = new Conseil(
                        rs.getInt("id_article"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie"),
                        rs.getTimestamp("date_creation"),
                        rs.getString("Auteur")
                );
                list.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur SELECT article_conseille: " + e.getMessage(), e);
        }

        return list;
    }

    @Override
    public Conseil getConseilById(int idArticle) {
        String sql = "SELECT id_article, titre, contenu, categorie, date_creation, Auteur " +
                "FROM article_conseille WHERE id_article=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idArticle);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Conseil(
                            rs.getInt("id_article"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getString("categorie"),
                            rs.getTimestamp("date_creation"),
                            rs.getString("Auteur")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getConseilById: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Conseil> rechercher(String keyword) {
        List<Conseil> list = new ArrayList<>();
        String k = (keyword == null) ? "" : keyword.trim();
        if (k.isEmpty()) return afficherConseils();

        String sql = "SELECT id_article, titre, contenu, categorie, date_creation, Auteur " +
                "FROM article_conseille " +
                "WHERE LOWER(titre) LIKE ? OR LOWER(contenu) LIKE ? OR LOWER(Auteur) LIKE ? " +
                "ORDER BY date_creation DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + k.toLowerCase() + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Conseil(
                            rs.getInt("id_article"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getString("categorie"),
                            rs.getTimestamp("date_creation"),
                            rs.getString("Auteur")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche article_conseille: " + e.getMessage(), e);
        }

        return list;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
