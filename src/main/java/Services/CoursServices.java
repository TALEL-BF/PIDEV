package Services;

import Entites.Cours;
import IServices.IServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursServices implements IServices<Cours> {

    private Connection con;

    public CoursServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    // ================== AJOUTER ==================
    @Override
    public boolean ajouter(Cours cours) {

        String req = "INSERT INTO Cours (titre, description, type_cours, niveau, duree, image) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(req)) {

            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setString(3, cours.getType_cours());
            ps.setString(4, cours.getNiveau());
            ps.setInt(5, cours.getDuree());
            ps.setString(6, cours.getImage());  // Changé de difficulte à image

            ps.executeUpdate();
            System.out.println("✅ Cours ajouté avec succès !");
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout cours : " + e.getMessage());
            return false;
        }
    }

    // ================== MODIFIER ==================
    @Override
    public boolean modifier(Cours cours) {

        String req = "UPDATE Cours SET titre=?, description=?, type_cours=?, niveau=?, duree=?, image=? WHERE id_cours=?";

        try (PreparedStatement ps = con.prepareStatement(req)) {

            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setString(3, cours.getType_cours());
            ps.setString(4, cours.getNiveau());
            ps.setInt(5, cours.getDuree());
            ps.setString(6, cours.getImage());  // Changé de difficulte à image
            ps.setInt(7, cours.getId_cours());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Cours modifié avec succès !");
                return true;
            } else {
                System.out.println("⚠️ Aucun cours trouvé avec l'ID : " + cours.getId_cours());
                return false;
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification : " + e.getMessage());
            return false;
        }
    }

    // ================== SUPPRIMER ==================
    @Override
    public boolean supprimer(int id) {

        String req = "DELETE FROM Cours WHERE id_cours=?";

        try (PreparedStatement ps = con.prepareStatement(req)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Cours supprimé !");
                return true;
            } else {
                System.out.println("⚠️ Aucun cours trouvé avec l'ID : " + id);
                return false;
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression : " + e.getMessage());
            return false;
        }
    }

    // ================== AFFICHER ==================
    @Override
    public List<Cours> getAll() {

        List<Cours> coursList = new ArrayList<>();
        String req = "SELECT * FROM Cours";

        try (Statement ste = con.createStatement();
             ResultSet rs = ste.executeQuery(req)) {

            while (rs.next()) {

                Cours cours = new Cours(
                        rs.getInt("id_cours"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("type_cours"),
                        rs.getString("niveau"),
                        rs.getInt("duree"),
                        rs.getString("image")  // Changé de difficulte à image
                );

                coursList.add(cours);
            }

            System.out.println("✅ " + coursList.size() + " cours chargés");

        } catch (SQLException e) {
            System.out.println("❌ Erreur affichage : " + e.getMessage());
        }

        return coursList;
    }

    // ================== MÉTHODES SUPPLÉMENTAIRES UTILES ==================

    public Cours getById(int id) {
        String req = "SELECT * FROM Cours WHERE id_cours = ?";

        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Cours(
                        rs.getInt("id_cours"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("type_cours"),
                        rs.getString("niveau"),
                        rs.getInt("duree"),
                        rs.getString("image")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur recherche par ID : " + e.getMessage());
        }
        return null;
    }

    public List<Cours> getByNiveau(String niveau) {
        List<Cours> coursList = new ArrayList<>();
        String req = "SELECT * FROM Cours WHERE niveau = ?";

        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, niveau);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Cours cours = new Cours(
                        rs.getInt("id_cours"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("type_cours"),
                        rs.getString("niveau"),
                        rs.getInt("duree"),
                        rs.getString("image")
                );
                coursList.add(cours);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur recherche par niveau : " + e.getMessage());
        }
        return coursList;
    }
}