package Services;

import Entites.Cours;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoursServicesTest {

    private static CoursServices coursServices;
    private static int idCoursTest;

    @BeforeAll
    static void setUp() {
        coursServices = new CoursServices();
        System.out.println("🔄 Initialisation des tests CoursServices...");
    }

    @Test
    @Order(1)
    void testAjouterCours() {
        Cours cours = new Cours(
                "Test Cours Unitaire",
                "Ceci est une description de test pour le cours unitaire",
                "Académique",
                "Débutant",
                60,
                "test_image.png",
                "MOT1;MOT2;MOT3",
                "image1.png;image2.png;image3.png"
        );

        // Exécution
        boolean result = coursServices.ajouter(cours);

        // Vérification
        assertTrue(result, "❌ L'ajout du cours devrait réussir");

        // Récupérer l'ID du cours ajouté pour les tests suivants
        List<Cours> coursList = coursServices.getAll();
        if (!coursList.isEmpty()) {
            idCoursTest = coursList.get(coursList.size() - 1).getId_cours();
            System.out.println("✅ Cours ajouté avec ID: " + idCoursTest);
        }
    }

    @Test
    @Order(2)
    void testAfficherCours() {
        // Exécution
        List<Cours> coursList = coursServices.getAll();

        // Vérifications
        assertNotNull(coursList, "❌ La liste des cours ne devrait pas être null");
        assertFalse(coursList.isEmpty(), "❌ La liste des cours ne devrait pas être vide");

        System.out.println("✅ Affichage des cours réussi - " + coursList.size() + " cours trouvés");
    }

    @Test
    @Order(3)
    void testGetById() {
        // Vérifier que nous avons un ID valide
        assertTrue(idCoursTest > 0, "❌ L'ID du cours de test devrait être valide");

        // Exécution
        Cours cours = coursServices.getById(idCoursTest);

        // Vérifications
        assertNotNull(cours, "❌ Le cours devrait être trouvé avec l'ID: " + idCoursTest);
        assertEquals("Test Cours Unitaire", cours.getTitre(), "❌ Le titre du cours ne correspond pas");
        assertEquals("Académique", cours.getType_cours(), "❌ Le type du cours ne correspond pas");
        assertEquals("Débutant", cours.getNiveau(), "❌ Le niveau du cours ne correspond pas");
        assertEquals(60, cours.getDuree(), "❌ La durée du cours ne correspond pas");

        System.out.println("✅ Récupération par ID réussie pour le cours: " + cours.getTitre());
    }

    @Test
    @Order(4)
    void testModifierCours() {
        // Vérifier que nous avons un ID valide
        assertTrue(idCoursTest > 0, "❌ L'ID du cours de test devrait être valide");

        // Récupérer le cours
        Cours cours = coursServices.getById(idCoursTest);
        assertNotNull(cours, "❌ Le cours à modifier devrait exister");

        // Modifier les données
        cours.setTitre("Test Cours Modifié");
        cours.setDescription("Description modifiée pour le test");
        cours.setType_cours("Social");
        cours.setNiveau("Intermédiaire");
        cours.setDuree(90);
        cours.setImage("image_modifiee.png");
        cours.setMots("MOT_MODIFIE1;MOT_MODIFIE2");
        cours.setImages_mots("img_mod1.png;img_mod2.png");

        // Exécution
        boolean result = coursServices.modifier(cours);

        // Vérification
        assertTrue(result, "❌ La modification du cours devrait réussir");

        System.out.println("✅ Modification du cours réussie");
    }

    @Test
    @Order(5)
    void testVerifierModification() {
        // Vérifier que les modifications sont appliquées
        Cours coursModifie = coursServices.getById(idCoursTest);
        assertNotNull(coursModifie, "❌ Le cours modifié devrait exister");
        assertEquals("Test Cours Modifié", coursModifie.getTitre(), "❌ Le titre n'a pas été modifié correctement");
        assertEquals("Social", coursModifie.getType_cours(), "❌ Le type n'a pas été modifié correctement");
        assertEquals("Intermédiaire", coursModifie.getNiveau(), "❌ Le niveau n'a pas été modifié correctement");
        assertEquals(90, coursModifie.getDuree(), "❌ La durée n'a pas été modifiée correctement");

        System.out.println("✅ Vérification de la modification réussie");
    }

    @Test
    @Order(6)
    void testSupprimerCours() {
        // Vérifier que nous avons un ID valide
        assertTrue(idCoursTest > 0, "❌ L'ID du cours de test devrait être valide");

        // Exécution
        boolean result = coursServices.supprimer(idCoursTest);

        // Vérification
        assertTrue(result, "❌ La suppression du cours devrait réussir");

        // Vérifier que le cours n'existe plus
        Cours coursSupprime = coursServices.getById(idCoursTest);
        assertNull(coursSupprime, "❌ Le cours ne devrait plus exister après suppression");

        System.out.println("✅ Suppression du cours réussie pour l'ID: " + idCoursTest);
    }

    @AfterEach
    void cleanUp() {
        System.out.println("🧹 Nettoyage après test...");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("✅ Tests CoursServices terminés");
    }
}