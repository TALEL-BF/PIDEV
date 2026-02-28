package Services;

import Entites.Cours;
import Entites.Evaluation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EvaluationServicesTest {

    private static EvaluationServices evaluationServices;
    private static CoursServices coursServices;
    private static int idCoursTest;
    private static int idEvaluationTest;

    @BeforeAll
    static void setUp() {
        evaluationServices = new EvaluationServices();
        coursServices = new CoursServices();
        System.out.println("🔄 Initialisation des tests EvaluationServices...");

        // Créer un cours de test pour associer les évaluations
        Cours coursTest = new Cours(
                "Cours pour Test Evaluation",
                "Description du cours pour les tests d'évaluation",
                "Académique",
                "Débutant",
                45,
                "cours_test.png",
                "MOT_TEST",
                "image_test.png"
        );

        boolean coursCree = coursServices.ajouter(coursTest);
        if (coursCree) {
            List<Cours> coursList = coursServices.getAll();
            if (!coursList.isEmpty()) {
                idCoursTest = coursList.get(coursList.size() - 1).getId_cours();
                System.out.println("✅ Cours de test créé avec ID: " + idCoursTest);
            }
        }
    }

    @Test
    @Order(1)
    void testAjouterEvaluation() {
        // Vérifier que le cours de test existe
        assertTrue(idCoursTest > 0, "❌ Le cours de test devrait être créé");

        // Création d'une évaluation de test
        Evaluation evaluation = new Evaluation();
        evaluation.setId_cours(idCoursTest);
        evaluation.setQuestion("Quelle est la couleur du ciel ?");
        evaluation.setChoix1("Bleu");
        evaluation.setChoix2("Vert");
        evaluation.setChoix3("Rouge");
        evaluation.setBonne_reponse("Bleu");
        evaluation.setScore(2);

        // Exécution
        boolean result = evaluationServices.ajouter(evaluation);

        // Vérification
        assertTrue(result, "❌ L'ajout de l'évaluation devrait réussir");
        assertTrue(evaluation.getId_eval() > 0, "❌ L'ID de l'évaluation devrait être généré");

        idEvaluationTest = evaluation.getId_eval();
        System.out.println("✅ Évaluation ajoutée avec ID: " + idEvaluationTest);
    }

    @Test
    @Order(2)
    void testAjouterPlusieursEvaluations() {
        // Vérifier que le cours de test existe
        assertTrue(idCoursTest > 0, "❌ Le cours de test devrait être créé");

        // Création de plusieurs évaluations
        List<Evaluation> evaluations = new ArrayList<>();
        evaluations.add(createEvaluation("Question 1", "Réponse A", "Réponse B", "Réponse C", "Réponse A", 1));
        evaluations.add(createEvaluation("Question 2", "Choix 1", "Choix 2", "Choix 3", "Choix 2", 2));
        evaluations.add(createEvaluation("Question 3", "Option X", "Option Y", "Option Z", "Option Z", 3));

        // Exécution
        boolean result = evaluationServices.ajouterPlusieurs(evaluations);

        // Vérification
        assertTrue(result, "❌ L'ajout multiple d'évaluations devrait réussir");

        System.out.println("✅ " + evaluations.size() + " évaluations ajoutées en lot");
    }

    @Test
    @Order(3)
    void testGetAllEvaluations() {
        // Exécution
        List<Evaluation> evaluations = evaluationServices.getAll();

        // Vérifications
        assertNotNull(evaluations, "❌ La liste des évaluations ne devrait pas être null");
        assertFalse(evaluations.isEmpty(), "❌ La liste des évaluations ne devrait pas être vide");

        System.out.println("✅ Récupération de toutes les évaluations: " + evaluations.size() + " trouvées");
    }

    @Test
    @Order(4)
    void testGetByCoursId() {
        // Vérifier que le cours de test existe
        assertTrue(idCoursTest > 0, "❌ Le cours de test devrait être créé");

        // Exécution
        List<Evaluation> evaluations = evaluationServices.getByCoursId(idCoursTest);

        // Vérifications
        assertNotNull(evaluations, "❌ La liste des évaluations par cours ne devrait pas être null");
        assertFalse(evaluations.isEmpty(), "❌ Le cours devrait avoir des évaluations");

        System.out.println("✅ Récupération des évaluations par cours: " + evaluations.size() + " trouvées");
    }

    @Test
    @Order(5)
    void testGetById() {
        // Vérifier que nous avons un ID valide
        assertTrue(idEvaluationTest > 0, "❌ L'ID de l'évaluation de test devrait être valide");

        // Exécution
        Evaluation evaluation = evaluationServices.getById(idEvaluationTest);

        // Vérifications
        assertNotNull(evaluation, "❌ L'évaluation devrait être trouvée avec l'ID: " + idEvaluationTest);
        assertEquals(idCoursTest, evaluation.getId_cours(), "❌ L'ID du cours ne correspond pas");
        assertEquals("Quelle est la couleur du ciel ?", evaluation.getQuestion(), "❌ La question ne correspond pas");
        assertEquals("Bleu", evaluation.getBonne_reponse(), "❌ La bonne réponse ne correspond pas");
        assertEquals(2, evaluation.getScore(), "❌ Le score ne correspond pas");

        System.out.println("✅ Récupération par ID réussie");
    }

    @Test
    @Order(6)
    void testModifierEvaluation() {
        // Vérifier que nous avons un ID valide
        assertTrue(idEvaluationTest > 0, "❌ L'ID de l'évaluation de test devrait être valide");

        // Récupérer l'évaluation
        Evaluation evaluation = evaluationServices.getById(idEvaluationTest);
        assertNotNull(evaluation, "❌ L'évaluation à modifier devrait exister");

        // Modifier les données
        evaluation.setQuestion("Quelle est la couleur du soleil ?");
        evaluation.setChoix1("Jaune");
        evaluation.setChoix2("Bleu");
        evaluation.setChoix3("Rouge");
        evaluation.setBonne_reponse("Jaune");
        evaluation.setScore(5);

        // Exécution
        boolean result = evaluationServices.modifier(evaluation);

        // Vérification
        assertTrue(result, "❌ La modification de l'évaluation devrait réussir");

        System.out.println("✅ Modification de l'évaluation réussie");
    }

    @Test
    @Order(7)
    void testVerifierModification() {
        // Vérifier que les modifications sont appliquées
        Evaluation evaluationModifiee = evaluationServices.getById(idEvaluationTest);
        assertNotNull(evaluationModifiee, "❌ L'évaluation modifiée devrait exister");
        assertEquals("Quelle est la couleur du soleil ?", evaluationModifiee.getQuestion(), "❌ La question n'a pas été modifiée");
        assertEquals("Jaune", evaluationModifiee.getBonne_reponse(), "❌ La bonne réponse n'a pas été modifiée");
        assertEquals(5, evaluationModifiee.getScore(), "❌ Le score n'a pas été modifié");

        System.out.println("✅ Vérification de la modification réussie");
    }

    @Test
    @Order(8)
    void testVerifierReponse() {
        // Vérifier que nous avons un ID valide
        assertTrue(idEvaluationTest > 0, "❌ L'ID de l'évaluation de test devrait être valide");

        // Test avec bonne réponse
        boolean reponseCorrecte = evaluationServices.verifierReponse(idEvaluationTest, "Jaune");
        assertTrue(reponseCorrecte, "❌ La vérification devrait retourner true pour la bonne réponse");

        // Test avec mauvaise réponse
        boolean reponseIncorrecte = evaluationServices.verifierReponse(idEvaluationTest, "Bleu");
        assertFalse(reponseIncorrecte, "❌ La vérification devrait retourner false pour une mauvaise réponse");

        System.out.println("✅ Vérification des réponses réussie");
    }

    @Test
    @Order(9)
    void testCompterParCours() {
        // Vérifier que le cours de test existe
        assertTrue(idCoursTest > 0, "❌ Le cours de test devrait être créé");

        // Exécution
        int count = evaluationServices.compterParCours(idCoursTest);

        // Vérification
        assertTrue(count > 0, "❌ Le cours devrait avoir au moins une évaluation");

        System.out.println("✅ Nombre d'évaluations pour le cours: " + count);
    }

    @Test
    @Order(10)
    void testGetScoreTotalParCours() {
        // Vérifier que le cours de test existe
        assertTrue(idCoursTest > 0, "❌ Le cours de test devrait être créé");

        // Exécution
        int totalScore = evaluationServices.getScoreTotalParCours(idCoursTest);

        // Vérification
        assertTrue(totalScore > 0, "❌ Le score total devrait être supérieur à 0");

        System.out.println("✅ Score total pour le cours: " + totalScore);
    }

    @Test
    @Order(11)
    void testSupprimerEvaluation() {
        // Vérifier que nous avons un ID valide
        assertTrue(idEvaluationTest > 0, "❌ L'ID de l'évaluation de test devrait être valide");

        // Exécution
        boolean result = evaluationServices.supprimer(idEvaluationTest);

        // Vérification
        assertTrue(result, "❌ La suppression de l'évaluation devrait réussir");

        System.out.println("✅ Suppression de l'évaluation réussie pour l'ID: " + idEvaluationTest);
    }

    @Test
    @Order(12)
    void testVerifierSuppression() {
        // Vérifier que l'évaluation n'existe plus
        Evaluation evaluationSupprimee = evaluationServices.getById(idEvaluationTest);
        assertNull(evaluationSupprimee, "❌ L'évaluation ne devrait plus exister après suppression");

        System.out.println("✅ Vérification de la suppression réussie");
    }

    @Test
    @Order(13)
    void testSupprimerParCours() {
        // Vérifier que le cours de test existe
        assertTrue(idCoursTest > 0, "❌ Le cours de test devrait être créé");

        // Exécution
        boolean result = evaluationServices.supprimerParCours(idCoursTest);

        // Vérification
        assertTrue(result, "❌ La suppression des évaluations par cours devrait réussir");

        // Vérifier qu'il n'y a plus d'évaluations pour ce cours
        int count = evaluationServices.compterParCours(idCoursTest);
        assertEquals(0, count, "❌ Le cours ne devrait plus avoir d'évaluations");

        System.out.println("✅ Suppression de toutes les évaluations du cours réussie");
    }

    // Méthode utilitaire pour créer une évaluation
    private Evaluation createEvaluation(String question, String choix1, String choix2,
                                        String choix3, String bonneReponse, int score) {
        Evaluation evaluation = new Evaluation();
        evaluation.setId_cours(idCoursTest);
        evaluation.setQuestion(question);
        evaluation.setChoix1(choix1);
        evaluation.setChoix2(choix2);
        evaluation.setChoix3(choix3);
        evaluation.setBonne_reponse(bonneReponse);
        evaluation.setScore(score);
        return evaluation;
    }

    @AfterEach
    void cleanUp() {
        System.out.println("🧹 Nettoyage après test...");
    }

    @AfterAll
    static void tearDown() {
        // Nettoyage final : supprimer le cours de test
        if (idCoursTest > 0) {
            evaluationServices.supprimerParCours(idCoursTest);
            coursServices.supprimer(idCoursTest);
            System.out.println("🧹 Nettoyage final : cours de test supprimé");
        }
        System.out.println("✅ Tests EvaluationServices terminés");
    }
}