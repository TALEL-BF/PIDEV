package Services;

import Entites.Evaluation;
import IServices.IevaluationServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationServices implements IevaluationServices<Evaluation> {

    private Connection con;

    public EvaluationServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Evaluation evaluation) {
        String req = "INSERT INTO evaluation(titre, description, type_evaluation, niveau, duree_minutes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, evaluation.getTitre());
            ps.setString(2, evaluation.getDescription());
            ps.setString(3, evaluation.getType_evaluation());
            ps.setString(4, evaluation.getNiveau());
            ps.setInt(5, evaluation.getDuree_minutes());

            ps.executeUpdate();
            System.out.println("Evaluation ajoutée avec succès !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean modifier(Evaluation evaluation) {
        String req = "UPDATE evaluation SET titre = ?, description = ?, type_evaluation = ?, niveau = ?, duree_minutes = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, evaluation.getTitre());
            ps.setString(2, evaluation.getDescription());
            ps.setString(3, evaluation.getType_evaluation());
            ps.setString(4, evaluation.getNiveau());
            ps.setInt(5, evaluation.getDuree_minutes());
            ps.setInt(6, evaluation.getId());

            ps.executeUpdate();
            System.out.println("Evaluation avec id = " + evaluation.getId() + " modifiée !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(int id) {
        String req = "DELETE FROM evaluation WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Evaluation avec id = " + id + " supprimée !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Evaluation> getAll() {
        List<Evaluation> evaluationList = new ArrayList<>();
        String req = "SELECT * FROM evaluation";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                Evaluation evaluation = new Evaluation(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("type_evaluation"),
                        rs.getString("niveau"),
                        rs.getInt("duree_minutes")
                );
                evaluationList.add(evaluation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluationList;
    }
}