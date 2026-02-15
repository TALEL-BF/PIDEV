package Services;

import Entites.Cours;
import Utils.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursServices {

    private Connection connection;

    public CoursServices() {
        connection = Mydatabase.getInstance().getConnection();
    }

    // Ajouter un cours
    public boolean ajouter(Cours cours) {
        String req = "INSERT INTO cours (titre, description, type_cours, niveau, duree, image, mots, images_mots) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setString(3, cours.getType_cours());
            ps.setString(4, cours.getNiveau());
            ps.setInt(5, cours.getDuree());
            ps.setString(6, cours.getImage());
            ps.setString(7, cours.getMots());
            ps.setString(8, cours.getImages_mots());

            System.out.println("📝 Ajout du cours: " + cours.getTitre());
            System.out.println("   Mots: " + cours.getMots());
            System.out.println("   Images_mots: " + cours.getImages_mots());

            int result = ps.executeUpdate();
            System.out.println("✅ Cours ajouté avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du cours : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Modifier un cours
    public boolean modifier(Cours cours) {
        String req = "UPDATE cours SET titre = ?, description = ?, type_cours = ?, niveau = ?, duree = ?, image = ?, mots = ?, images_mots = ? WHERE id_cours = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setString(3, cours.getType_cours());
            ps.setString(4, cours.getNiveau());
            ps.setInt(5, cours.getDuree());
            ps.setString(6, cours.getImage());
            ps.setString(7, cours.getMots());
            ps.setString(8, cours.getImages_mots());
            ps.setInt(9, cours.getId_cours());

            System.out.println("📝 Modification du cours ID: " + cours.getId_cours());
            System.out.println("   Mots: " + cours.getMots());
            System.out.println("   Images_mots: " + cours.getImages_mots());

            int result = ps.executeUpdate();
            System.out.println("✅ Cours modifié avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification du cours : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Récupérer tous les cours
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String req = "SELECT * FROM cours";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Cours cours = new Cours(
                        rs.getInt("id_cours"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("type_cours"),
                        rs.getString("niveau"),
                        rs.getInt("duree"),
                        rs.getString("image"),
                        rs.getString("mots"),
                        rs.getString("images_mots")
                );
                coursList.add(cours);

                // Log pour voir ce qui est chargé
                System.out.println("📚 Cours chargé: " + cours.getTitre());
                System.out.println("   Mots: " + cours.getMots());
                System.out.println("   Images_mots: " + cours.getImages_mots());
                System.out.println("---");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des cours : " + e.getMessage());
            e.printStackTrace();
        }
        return coursList;
    }

    // Récupérer un cours par son ID
    public Cours getById(int id) {
        String req = "SELECT * FROM cours WHERE id_cours = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Cours cours = new Cours(
                        rs.getInt("id_cours"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("type_cours"),
                        rs.getString("niveau"),
                        rs.getInt("duree"),
                        rs.getString("image"),
                        rs.getString("mots"),
                        rs.getString("images_mots")
                );

                System.out.println("🔍 Cours trouvé ID " + id + ": " + cours.getTitre());
                System.out.println("   Mots: " + cours.getMots());
                System.out.println("   Images_mots: " + cours.getImages_mots());

                return cours;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du cours : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Supprimer un cours
    public boolean supprimer(int id) {
        String req = "DELETE FROM cours WHERE id_cours = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            System.out.println("✅ Cours supprimé avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du cours : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}