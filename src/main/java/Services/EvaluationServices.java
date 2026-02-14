package Services;

import Entites.Cours;
import Entites.Evaluation;
import Utils.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationServices {
    private Connection connection;
    private CoursServices coursServices;

    public EvaluationServices() {
        connection = Mydatabase.getInstance().getConnection();
        coursServices = new CoursServices();
    }

    // Ajouter une question d'évaluation
    public boolean ajouter(Evaluation evaluation) {
        String req = "INSERT INTO evaluation (id_cours, question, choix1, choix2, choix3, bonne_reponse, score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, evaluation.getId_cours());
            ps.setString(2, evaluation.getQuestion());
            ps.setString(3, evaluation.getChoix1());
            ps.setString(4, evaluation.getChoix2());
            ps.setString(5, evaluation.getChoix3());
            ps.setString(6, evaluation.getBonne_reponse());
            ps.setInt(7, evaluation.getScore());

            int result = ps.executeUpdate();

            if (result > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    evaluation.setId_eval(rs.getInt(1));
                }
                System.out.println("✅ Question ajoutée avec succès !");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
        return false;
    }

    // Ajouter plusieurs questions pour un cours
    public boolean ajouterPlusieurs(List<Evaluation> evaluations) {
        boolean allSuccess = true;
        for (Evaluation e : evaluations) {
            if (!ajouter(e)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    // Récupérer toutes les évaluations
    public List<Evaluation> getAll() {
        List<Evaluation> evaluations = new ArrayList<>();
        String req = "SELECT e.*, c.* FROM evaluation e " +
                "JOIN cours c ON e.id_cours = c.id_cours";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Evaluation evaluation = extractEvaluationFromResultSet(rs);
                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération : " + e.getMessage());
        }
        return evaluations;
    }

    // Récupérer les évaluations par ID de cours
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
                Evaluation evaluation = extractEvaluationFromResultSet(rs);
                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération par cours : " + e.getMessage());
        }
        return evaluations;
    }

    // Récupérer une évaluation par ID
    public Evaluation getById(int id) {
        String req = "SELECT e.*, c.* FROM evaluation e " +
                "JOIN cours c ON e.id_cours = c.id_cours " +
                "WHERE e.id_eval = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractEvaluationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération par ID : " + e.getMessage());
        }
        return null;
    }

    // Modifier une évaluation
    public boolean modifier(Evaluation evaluation) {
        String req = "UPDATE evaluation SET question = ?, choix1 = ?, choix2 = ?, " +
                "choix3 = ?, bonne_reponse = ?, score = ? WHERE id_eval = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, evaluation.getQuestion());
            ps.setString(2, evaluation.getChoix1());
            ps.setString(3, evaluation.getChoix2());
            ps.setString(4, evaluation.getChoix3());
            ps.setString(5, evaluation.getBonne_reponse());
            ps.setInt(6, evaluation.getScore());
            ps.setInt(7, evaluation.getId_eval());

            int result = ps.executeUpdate();
            System.out.println("✅ Question modifiée avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur modification : " + e.getMessage());
            return false;
        }
    }

    // Supprimer une évaluation
    public boolean supprimer(int id) {
        String req = "DELETE FROM evaluation WHERE id_eval = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            System.out.println("✅ Question supprimée avec succès !");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression : " + e.getMessage());
            return false;
        }
    }

    // Supprimer toutes les évaluations d'un cours
    public boolean supprimerParCours(int coursId) {
        String req = "DELETE FROM evaluation WHERE id_cours = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, coursId);
            ps.executeUpdate();
            System.out.println("✅ Toutes les questions du cours supprimées");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression par cours : " + e.getMessage());
            return false;
        }
    }

    // Compter le nombre de questions pour un cours
    public int compterParCours(int coursId) {
        String req = "SELECT COUNT(*) FROM evaluation WHERE id_cours = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage : " + e.getMessage());
        }
        return 0;
    }

    // Calculer le score total pour un cours
    public int getScoreTotalParCours(int coursId) {
        String req = "SELECT SUM(score) FROM evaluation WHERE id_cours = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur calcul score total : " + e.getMessage());
        }
        return 0;
    }

    // Vérifier si une réponse est correcte
    public boolean verifierReponse(int evaluationId, String reponse) {
        Evaluation e = getById(evaluationId);
        return e != null && e.getBonne_reponse().equals(reponse);
    }

    // Extraire une évaluation du ResultSet
    private Evaluation extractEvaluationFromResultSet(ResultSet rs) throws SQLException {
        Evaluation evaluation = new Evaluation();
        evaluation.setId_eval(rs.getInt("e.id_eval"));
        evaluation.setId_cours(rs.getInt("e.id_cours"));
        evaluation.setQuestion(rs.getString("e.question"));
        evaluation.setChoix1(rs.getString("e.choix1"));
        evaluation.setChoix2(rs.getString("e.choix2"));
        evaluation.setChoix3(rs.getString("e.choix3"));
        evaluation.setBonne_reponse(rs.getString("e.bonne_reponse"));
        evaluation.setScore(rs.getInt("e.score"));

        // Créer l'objet Cours associé
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
        evaluation.setCours(cours);

        return evaluation;
    }
}