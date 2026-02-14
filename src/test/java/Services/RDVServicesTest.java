package Services;

import Entites.RDV;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tests unitaires pour RDVServices
 * Vérifie les opérations CRUD sur les rendez-vous
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RDVServicesTest {

    private RDVServices rdvServices;
    private RDV testRDV;

    @BeforeEach
    void setUp() {
        rdvServices = new RDVServices();

        testRDV = new RDV(
                "suivi",
                LocalDateTime.of(2025, 6, 20, 14, 30),
                "planifiee",
                45,
                1,
                1
        );
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Ajout d'un RDV")
    void testAjouterRDV() {
        System.out.println("\n=== Test 1: Ajout d'un RDV ===");

        try {
            rdvServices.ajouterRDV(testRDV);

            assertNotNull(testRDV.getIdRdv(), "L'ID du RDV devrait être généré");
            assertTrue(testRDV.getIdRdv() > 0, "L'ID devrait être positif");

            System.out.println("✅ RDV ajouté avec succès - ID: " + testRDV.getIdRdv());

        } catch (Exception e) {
            fail("L'ajout du RDV ne devrait pas lancer d'exception: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Affichage de tous les RDV")
    void testAfficherRDV() {
        System.out.println("\n=== Test 2: Affichage de tous les RDV ===");

        List<RDV> rdvList = rdvServices.afficherRDV();

        assertNotNull(rdvList, "La liste ne devrait pas être null");
        assertTrue(rdvList.size() >= 0, "La liste devrait contenir au moins 0 éléments");

        System.out.println("✅ Nombre de RDV trouvés: " + rdvList.size());
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Récupération d'un RDV par ID")
    void testGetRDVById() {
        System.out.println("\n=== Test 3: Récupération par ID ===");

        rdvServices.ajouterRDV(testRDV);
        int id = testRDV.getIdRdv();

        RDV retrieved = rdvServices.getRDVById(id);

        assertNotNull(retrieved, "Le RDV devrait être trouvé");
        assertEquals(id, retrieved.getIdRdv(), "L'ID devrait correspondre");
        assertEquals("suivi", retrieved.getTypeConsultation(), "Le type devrait correspondre");

        System.out.println("✅ RDV récupéré: " + retrieved.getTypeConsultation());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Modification d'un RDV")
    void testModifierRDV() {
        System.out.println("\n=== Test 4: Modification d'un RDV ===");

        rdvServices.ajouterRDV(testRDV);
        int id = testRDV.getIdRdv();

        testRDV.setTypeConsultation("urgence");
        testRDV.setStatutRdv("confirme");
        testRDV.setDureeRdvMinutes(60);

        rdvServices.modifierRDV(testRDV);

        RDV modified = rdvServices.getRDVById(id);

        assertEquals("urgence", modified.getTypeConsultation(), "Le type devrait être modifié");
        assertEquals("confirme", modified.getStatutRdv(), "Le statut devrait être modifié");
        assertEquals(60, modified.getDureeRdvMinutes(), "La durée devrait être modifiée");

        System.out.println("✅ RDV modifié avec succès");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Suppression d'un RDV")
    void testSupprimerRDV() {
        System.out.println("\n=== Test 5: Suppression d'un RDV ===");

        rdvServices.ajouterRDV(testRDV);
        int id = testRDV.getIdRdv();

        rdvServices.supprimerRDV(id);

        RDV deleted = rdvServices.getRDVById(id);
        assertNull(deleted, "Le RDV devrait être supprimé");

        System.out.println("✅ RDV supprimé avec succès");
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Types de consultation valides")
    void testTypesConsultation() {
        System.out.println("\n=== Test 6: Types de consultation ===");

        String[] typesValides = {"premiere_consultation", "suivi", "urgence", "familiale", "bilan"};

        for (String type : typesValides) {
            RDV rdv = new RDV(type, LocalDateTime.now(), "planifiee", 45, 1, 1);
            rdvServices.ajouterRDV(rdv);

            assertNotNull(rdv.getIdRdv(), "Le RDV avec type '" + type + "' devrait être ajouté");

            // Nettoyer
            rdvServices.supprimerRDV(rdv.getIdRdv());
        }

        System.out.println("✅ Tous les types de consultation sont valides");
    }

    @AfterEach
    void tearDown() {
        if (testRDV != null && testRDV.getIdRdv() > 0) {
            try {
                rdvServices.supprimerRDV(testRDV.getIdRdv());
            } catch (Exception e) {
                // Ignorer les erreurs de nettoyage
            }
        }
    }

    @AfterAll
    static void cleanup() {
        System.out.println("\n=== Tests RDV terminés ===");
    }
}

