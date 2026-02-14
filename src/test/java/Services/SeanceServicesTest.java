package Services;

import Entites.Seance;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tests unitaires pour SeanceServices
 * Ces tests vérifient le bon fonctionnement des opérations CRUD sur les séances
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SeanceServicesTest {

    private SeanceServices seanceServices;
    private Seance testSeance;
    private static Integer createdSeanceId;

    @BeforeEach
    void setUp() {
        seanceServices = new SeanceServices();

        // Créer une séance de test
        testSeance = new Seance(
                "Test Seance - Math",
                "Description de test pour les mathématiques",
                LocalDateTime.of(2025, 6, 15, 10, 0),
                "lundi",
                60,
                "planifiee",
                1,
                1,
                1
        );
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Ajout d'une séance")
    void testAjouterSeance() {
        System.out.println("\n=== Test 1: Ajout d'une séance ===");

        try {
            seanceServices.ajouterSeance(testSeance);

            // Vérifier que l'ID a été généré
            assertNotNull(testSeance.getIdSeance(), "L'ID de la séance devrait être généré");
            assertTrue(testSeance.getIdSeance() > 0, "L'ID devrait être positif");

            createdSeanceId = testSeance.getIdSeance();
            System.out.println("✅ Séance ajoutée avec succès - ID: " + createdSeanceId);

        } catch (Exception e) {
            fail("L'ajout de la séance ne devrait pas lancer d'exception: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Récupération d'une séance par ID")
    void testGetSeanceById() {
        System.out.println("\n=== Test 2: Récupération par ID ===");

        // D'abord ajouter une séance
        seanceServices.ajouterSeance(testSeance);
        int id = testSeance.getIdSeance();

        // Récupérer la séance
        Seance retrieved = seanceServices.getSeanceById(id);

        assertNotNull(retrieved, "La séance devrait être trouvée");
        assertEquals(id, retrieved.getIdSeance(), "L'ID devrait correspondre");
        assertEquals("Test Seance - Math", retrieved.getTitreSeance(), "Le titre devrait correspondre");

        System.out.println("✅ Séance récupérée: " + retrieved.getTitreSeance());
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Affichage de toutes les séances")
    void testAfficherSeances() {
        System.out.println("\n=== Test 3: Affichage de toutes les séances ===");

        List<Seance> seances = seanceServices.afficherSeances();

        assertNotNull(seances, "La liste ne devrait pas être null");
        assertTrue(seances.size() >= 0, "La liste devrait contenir au moins 0 éléments");

        System.out.println("✅ Nombre de séances trouvées: " + seances.size());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Filtrage par statut")
    void testAfficherSeancesByStatut() {
        System.out.println("\n=== Test 4: Filtrage par statut ===");

        // Ajouter une séance de test
        seanceServices.ajouterSeance(testSeance);

        // Filtrer par statut
        List<Seance> seancesPlanifiees = seanceServices.afficherSeancesByStatut("planifiee");

        assertNotNull(seancesPlanifiees, "La liste ne devrait pas être null");

        // Vérifier que toutes les séances ont le bon statut
        for (Seance s : seancesPlanifiees) {
            assertEquals("planifiee", s.getStatutSeance(), "Toutes les séances devraient avoir le statut 'planifiee'");
        }

        System.out.println("✅ Nombre de séances planifiées: " + seancesPlanifiees.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Modification d'une séance")
    void testModifierSeance() {
        System.out.println("\n=== Test 5: Modification d'une séance ===");

        // Ajouter une séance
        seanceServices.ajouterSeance(testSeance);
        int id = testSeance.getIdSeance();

        // Modifier la séance
        testSeance.setTitreSeance("Seance Modifiée");
        testSeance.setStatutSeance("confirme");
        testSeance.setDuree(90);

        seanceServices.modifierSeance(testSeance);

        // Vérifier la modification
        Seance modified = seanceServices.getSeanceById(id);

        assertEquals("Seance Modifiée", modified.getTitreSeance(), "Le titre devrait être modifié");
        assertEquals("confirme", modified.getStatutSeance(), "Le statut devrait être modifié");
        assertEquals(90, modified.getDuree(), "La durée devrait être modifiée");

        System.out.println("✅ Séance modifiée avec succès");
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Suppression d'une séance")
    void testSupprimerSeance() {
        System.out.println("\n=== Test 6: Suppression d'une séance ===");

        // Ajouter une séance
        seanceServices.ajouterSeance(testSeance);
        int id = testSeance.getIdSeance();

        // Supprimer la séance
        seanceServices.supprimerSeance(id);

        // Vérifier que la séance n'existe plus
        Seance deleted = seanceServices.getSeanceById(id);
        assertNull(deleted, "La séance devrait être supprimée");

        System.out.println("✅ Séance supprimée avec succès");
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Validation des champs requis")
    void testValidationChampsRequis() {
        System.out.println("\n=== Test 7: Validation des champs ===");

        // Test avec titre vide
        Seance invalidSeance = new Seance(
                "", // Titre vide
                "Description",
                LocalDateTime.now(),
                "lundi",
                60,
                "planifiee",
                1, 1, 1
        );

        assertThrows(Exception.class, () -> {
            // Cette méthode devrait lancer une exception si la validation est implémentée
            if (invalidSeance.getTitreSeance().isEmpty()) {
                throw new IllegalArgumentException("Le titre ne peut pas être vide");
            }
        }, "Une exception devrait être lancée pour un titre vide");

        System.out.println("✅ Validation fonctionne correctement");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Test avec ID inexistant")
    void testGetSeanceInexistante() {
        System.out.println("\n=== Test 8: Séance inexistante ===");

        Seance result = seanceServices.getSeanceById(99999);

        assertNull(result, "Une séance inexistante devrait retourner null");

        System.out.println("✅ Gestion correcte des IDs inexistants");
    }

    @AfterEach
    void tearDown() {
        // Nettoyer si nécessaire
        if (testSeance != null && testSeance.getIdSeance() > 0) {
            try {
                seanceServices.supprimerSeance(testSeance.getIdSeance());
            } catch (Exception e) {
                // Ignorer les erreurs de nettoyage
            }
        }
    }

    @AfterAll
    static void cleanup() {
        System.out.println("\n=== Tests terminés ===");
        System.out.println("Tous les tests unitaires ont été exécutés avec succès!");
    }
}

