package Services;

import Entites.Sponsor;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import Entites.Event;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SponsorServicesTest {

    private static SponsorServices sponsorServices;
    private static int idSponsorTest;

    @BeforeAll
    static void setUp() {
        sponsorServices = new SponsorServices();
        System.out.println("🔄 Initialisation des tests SponsorServices...");
    }

    @Test
    @Order(1)
    void testAjouterSponsor() {
        // CREATE - Ajout d'un sponsor avec TOUS vos attributs (image incluse)
        Sponsor sponsor = new Sponsor(
                "TechCorp",
                "Technologie",
                "contact@techcorp.com",
                71234567,
                "Partenaire technologique officiel",
                "techcorp_logo.png"  // ⭐ IMAGE OBLIGATOIRE
        );

        sponsorServices.ajoutSponsor(sponsor);

        assertTrue(sponsor.getIdSponsor() > 0, "❌ L'ID du sponsor devrait être généré");
        idSponsorTest = sponsor.getIdSponsor();
        System.out.println("✅ Sponsor ajouté avec ID: " + idSponsorTest);
        System.out.println("   - Nom: " + sponsor.getNom());
        System.out.println("   - Type: " + sponsor.getTypeSponsor());
        System.out.println("   - Email: " + sponsor.getEmail());
        System.out.println("   - Téléphone: " + sponsor.getTelephone());
        System.out.println("   - Image: " + sponsor.getImage());
    }

    @Test
    @Order(2)
    void testAfficherSponsors() {
        // READ ALL - Afficher tous les sponsors
        List<Sponsor> sponsors = sponsorServices.afficherSponsor();

        assertNotNull(sponsors, "❌ La liste des sponsors ne devrait pas être null");
        assertFalse(sponsors.isEmpty(), "❌ La liste des sponsors ne devrait pas être vide");

        System.out.println("✅ Affichage des sponsors réussi - " + sponsors.size() + " sponsors trouvés");
        for (Sponsor s : sponsors) {
            System.out.println("   - " + s.getNom() + " (ID: " + s.getIdSponsor() + ", Type: " + s.getTypeSponsor() + ")");
        }
    }

    @Test
    @Order(3)
    void testGetSponsorById() {
        // READ BY ID - Récupérer un sponsor par son ID
        assertTrue(idSponsorTest > 0, "❌ L'ID du sponsor de test devrait être valide");

        Sponsor sponsor = sponsorServices.getSponsorById(idSponsorTest);

        assertNotNull(sponsor, "❌ Le sponsor devrait être trouvé avec l'ID: " + idSponsorTest);
        assertEquals("TechCorp", sponsor.getNom(), "❌ Le nom du sponsor ne correspond pas");
        assertEquals("Technologie", sponsor.getTypeSponsor(), "❌ Le type ne correspond pas");
        assertEquals("contact@techcorp.com", sponsor.getEmail(), "❌ L'email ne correspond pas");
        assertEquals(71234567, sponsor.getTelephone(), "❌ Le téléphone ne correspond pas");
        assertEquals("techcorp_logo.png", sponsor.getImage(), "❌ L'image ne correspond pas");

        System.out.println("✅ Récupération par ID réussie pour le sponsor: " + sponsor.getNom());
        System.out.println("   - Type: " + sponsor.getTypeSponsor());
        System.out.println("   - Email: " + sponsor.getEmail());
        System.out.println("   - Téléphone: " + sponsor.getTelephone());
        System.out.println("   - Image: " + sponsor.getImage());
    }

    @Test
    @Order(4)
    void testModifierSponsor() {
        // UPDATE - Modifier un sponsor
        assertTrue(idSponsorTest > 0, "❌ L'ID du sponsor de test devrait être valide");

        Sponsor sponsor = sponsorServices.getSponsorById(idSponsorTest);
        assertNotNull(sponsor, "❌ Le sponsor à modifier devrait exister");

        // Modifier toutes les données (image incluse)
        sponsor.setNom("TechCorp International");
        sponsor.setTypeSponsor("Technologie & Innovation");
        sponsor.setEmail("international@techcorp.com");
        sponsor.setTelephone(71234568);
        sponsor.setDescription("Partenaire technologique international majeur");
        sponsor.setImage("techcorp_international_logo.png");  // ⭐ MODIFICATION IMAGE

        sponsorServices.modifierSponsor(sponsor);

        System.out.println("✅ Modification du sponsor réussie");
    }

    @Test
    @Order(5)
    void testVerifierModification() {
        // VERIFY UPDATE - Vérifier que les modifications sont appliquées
        Sponsor sponsorModifie = sponsorServices.getSponsorById(idSponsorTest);

        assertNotNull(sponsorModifie, "❌ Le sponsor modifié devrait exister");
        assertEquals("TechCorp International", sponsorModifie.getNom(), "❌ Le nom n'a pas été modifié");
        assertEquals("Technologie & Innovation", sponsorModifie.getTypeSponsor(), "❌ Le type n'a pas été modifié");
        assertEquals("international@techcorp.com", sponsorModifie.getEmail(), "❌ L'email n'a pas été modifié");
        assertEquals(71234568, sponsorModifie.getTelephone(), "❌ Le téléphone n'a pas été modifié");
        assertEquals("techcorp_international_logo.png", sponsorModifie.getImage(), "❌ L'image n'a pas été modifiée");

        System.out.println("✅ Vérification de la modification réussie");
        System.out.println("   - Nouveau nom: " + sponsorModifie.getNom());
        System.out.println("   - Nouveau type: " + sponsorModifie.getTypeSponsor());
        System.out.println("   - Nouvel email: " + sponsorModifie.getEmail());
        System.out.println("   - Nouvelle image: " + sponsorModifie.getImage());
    }

    @Test
    @Order(6)
    void testRechercherSponsor() {
        // SEARCH - Rechercher des sponsors par mot-clé
        List<Sponsor> resultats = sponsorServices.rechercherSponsor("Tech");

        assertNotNull(resultats, "❌ La liste des résultats ne devrait pas être null");
        assertTrue(resultats.size() > 0, "❌ Au moins un sponsor devrait être trouvé");

        System.out.println("✅ Recherche par mot-clé réussie - " + resultats.size() + " sponsors trouvés");
        for (Sponsor s : resultats) {
            System.out.println("   - " + s.getNom() + " (Type: " + s.getTypeSponsor() + ")");
        }
    }

    @Test
    @Order(7)
    void testGetTopSponsors() {
        // TOP SPONSORS - Récupérer les meilleurs sponsors
        List<Sponsor> topSponsors = sponsorServices.getTopSponsors();

        assertNotNull(topSponsors, "❌ La liste des top sponsors ne devrait pas être null");

        System.out.println("✅ Récupération des top sponsors réussie - " + topSponsors.size() + " sponsors");
        for (Sponsor s : topSponsors) {
            System.out.println("   - " + s.getNom() + " (ID: " + s.getIdSponsor() + ")");
        }
    }

    @Test
    @Order(8)
    void testGetEventsForSponsor() {
        // EVENTS FOR SPONSOR - Récupérer les événements d'un sponsor
        assertTrue(idSponsorTest > 0, "❌ L'ID du sponsor de test devrait être valide");

        List<Event> events = sponsorServices.getEventsForSponsor(idSponsorTest);

        assertNotNull(events, "❌ La liste des événements ne devrait pas être null");

        System.out.println("✅ Récupération des événements du sponsor réussie - " + events.size() + " événements");
        for (Event e : events) {
            System.out.println("   - " + e.getTitre() + " (ID: " + e.getIdEvent() + ")");
        }
    }

    @Test
    @Order(9)
    void testSupprimerSponsor() {
        // DELETE - Supprimer un sponsor
        assertTrue(idSponsorTest > 0, "❌ L'ID du sponsor de test devrait être valide");

        sponsorServices.supprimerSponsor(idSponsorTest);

        // Vérifier que le sponsor n'existe plus
        Sponsor sponsorSupprime = sponsorServices.getSponsorById(idSponsorTest);
        assertNull(sponsorSupprime, "❌ Le sponsor ne devrait plus exister après suppression");

        System.out.println("✅ Suppression du sponsor réussie pour l'ID: " + idSponsorTest);
    }

    @AfterEach
    void cleanUp() {
        System.out.println("🧹 Nettoyage après test...");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("✅ Tests SponsorServices terminés");
    }
}