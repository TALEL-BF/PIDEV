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
        // On récupère la connexion à la base
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
}