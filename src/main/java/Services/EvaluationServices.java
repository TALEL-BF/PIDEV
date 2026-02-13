package Services;

import Entites.Cours;
import Entites.Evaluation;
import Utils.Mydatabase;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EvaluationServices {
    private Connection connection;
    private CoursServices coursServices;

    public EvaluationServices() {
        connection = Mydatabase.getInstance().getConnection();
        coursServices = new CoursServices();
    }

    // Ajouter une évaluation
    public boolean ajouter(Evaluation evaluation) {
        String req = "INSERT INTO evaluation (type_evaluation, score, niveau_comprehension, date_evaluation, id_cours) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, evaluation.getType_evaluation());
            ps.setFloat(2, evaluation.getScore());
            ps.setString(3, evaluation.getNiveau_comprehension());
            ps.setDate(4, Date.valueOf(evaluation.getDate_evaluation()));
            ps.setInt(5, evaluation.getCours().getId_cours());

            int result = ps.executeUpdate();
            System.out.println("✅ Évaluation ajoutée avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'évaluation : " + e.getMessage());
            return false;
        }
    }

    // Récupérer toutes les évaluations (avec jointure)
    public List<Evaluation> getAll() {
        List<Evaluation> evaluations = new ArrayList<>();
        String req = "SELECT e.*, c.* FROM evaluation e " +
                "JOIN cours c ON e.id_cours = c.id_cours";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                // Créer l'objet Cours
                Cours cours = new Cours(
                        rs.getInt("c.id_cours"),
                        rs.getString("c.titre"),
                        rs.getString("c.description"),
                        rs.getString("c.type_cours"),
                        rs.getString("c.niveau"),
                        rs.getInt("c.duree"),
                        rs.getString("c.image"),
                        rs.getString("c.mots"),
                        rs.getString("c.images_mots")
                );

                // Créer l'objet Evaluation
                Evaluation evaluation = new Evaluation(
                        rs.getInt("e.id_evaluation"),
                        rs.getString("e.type_evaluation"),
                        rs.getFloat("e.score"),
                        rs.getString("e.niveau_comprehension"),
                        rs.getDate("e.date_evaluation").toLocalDate(),
                        cours
                );

                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des évaluations : " + e.getMessage());
        }
        return evaluations;
    }

    // Récupérer les évaluations pour un cours spécifique
    public List<Evaluation> getByCoursId(int coursId) {
        List<Evaluation> evaluations = new ArrayList<>();
        String req = "SELECT e.*, c.* FROM evaluation e " +
                "JOIN cours c ON e.id_cours = c.id_cours " +
                "WHERE e.id_cours = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Cours cours = new Cours(
                        rs.getInt("c.id_cours"),
                        rs.getString("c.titre"),
                        rs.getString("c.description"),
                        rs.getString("c.type_cours"),
                        rs.getString("c.niveau"),
                        rs.getInt("c.duree"),
                        rs.getString("c.image"),
                        rs.getString("c.mots"),
                        rs.getString("c.images_mots")
                );

                Evaluation evaluation = new Evaluation(
                        rs.getInt("e.id_evaluation"),
                        rs.getString("e.type_evaluation"),
                        rs.getFloat("e.score"),
                        rs.getString("e.niveau_comprehension"),
                        rs.getDate("e.date_evaluation").toLocalDate(),
                        cours
                );

                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des évaluations : " + e.getMessage());
        }
        return evaluations;
    }

    // Modifier une évaluation
    public boolean modifier(Evaluation evaluation) {
        String req = "UPDATE evaluation SET type_evaluation = ?, score = ?, " +
                "niveau_comprehension = ?, date_evaluation = ?, id_cours = ? " +
                "WHERE id_evaluation = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, evaluation.getType_evaluation());
            ps.setFloat(2, evaluation.getScore());
            ps.setString(3, evaluation.getNiveau_comprehension());
            ps.setDate(4, Date.valueOf(evaluation.getDate_evaluation()));
            ps.setInt(5, evaluation.getCours().getId_cours());
            ps.setInt(6, evaluation.getId_evaluation());

            int result = ps.executeUpdate();
            System.out.println("✅ Évaluation modifiée avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
            return false;
        }
    }

    // Supprimer une évaluation
    public boolean supprimer(int id) {
        String req = "DELETE FROM evaluation WHERE id_evaluation = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            System.out.println("✅ Évaluation supprimée avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }
}