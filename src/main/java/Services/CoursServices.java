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

    @Override
    public boolean ajouter(Cours cours) {
        String req = "INSERT INTO Cours(titre, description, type_cours, difficulte) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setString(3, cours.getType_cours());
            ps.setString(4, cours.getDifficulte());

            ps.executeUpdate();
            System.out.println("Cours ajouté avec succès !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean modifier(Cours cours) {
        String req = "UPDATE Cours SET titre = ?, description = ?, type_cours = ?, difficulte = ? WHERE id_cours = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setString(3, cours.getType_cours());
            ps.setString(4, cours.getDifficulte());
            ps.setInt(5, cours.getId_cours());

            ps.executeUpdate();
            System.out.println("Cours avec id = " + cours.getId_cours() + " modifié !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(int id) {
        String req = "DELETE FROM Cours WHERE id_cours = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Cours avec id = " + id + " supprimé !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String req = "SELECT * FROM Cours";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                Cours cours = new Cours(
                        rs.getInt("id_cours"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("type_cours"),
                        rs.getString("difficulte")
                );
                coursList.add(cours);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coursList;
    }
}
