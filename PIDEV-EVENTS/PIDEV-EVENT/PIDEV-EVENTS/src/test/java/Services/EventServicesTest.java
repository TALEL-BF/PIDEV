package Services;

import Entites.Event;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventServicesTest {

    private static EventServices eventServices;
    private static int idEventTest;

    @BeforeAll
    static void setUp() {
        eventServices = new EventServices();
        System.out.println("🔄 Initialisation des tests EventServices...");
    }

    @Test
    @Order(1)
    void testAjouterEvent() {
        // CREATE
        Event event = new Event();
        event.setTitre("Test Event Unitaire");
        event.setDescription("Description de test pour l'événement");
        event.setTypeEvent("Atelier");
        event.setLieu("Tunis");
        event.setMaxParticipant(20);
        event.setDateDebut(Date.valueOf(LocalDate.of(2026, 5, 10)));
        event.setDateFin(Date.valueOf(LocalDate.of(2026, 5, 10)));
        event.setHeureDebut(Time.valueOf(LocalTime.of(14, 0)));
        event.setHeureFin(Time.valueOf(LocalTime.of(17, 0)));
        event.setImage("test_event.png");

        eventServices.ajoutEvent(event);

        assertTrue(event.getIdEvent() > 0, "❌ L'ID devrait être généré");
        idEventTest = event.getIdEvent();
        System.out.println("✅ Événement ajouté avec ID: " + idEventTest);
    }

    @Test
    @Order(2)
    void testAfficherEvents() {
        // READ ALL
        List<Event> events = eventServices.afficherEvent();

        assertNotNull(events, "❌ Liste ne devrait pas être null");
        assertFalse(events.isEmpty(), "❌ Liste ne devrait pas être vide");
        System.out.println("✅ " + events.size() + " événements trouvés");
    }

    @Test
    @Order(3)
    void testGetEventById() {
        // READ BY ID
        assertTrue(idEventTest > 0, "❌ ID invalide");

        // Comme pas de getById, on filtre
        List<Event> events = eventServices.afficherEvent();
        Event event = events.stream()
                .filter(e -> e.getIdEvent() == idEventTest)
                .findFirst()
                .orElse(null);

        assertNotNull(event, "❌ Événement non trouvé");
        assertEquals("Test Event Unitaire", event.getTitre());
        assertEquals("Atelier", event.getTypeEvent());
        System.out.println("✅ Événement récupéré: " + event.getTitre());
    }

    @Test
    @Order(4)
    void testModifierEvent() {
        // UPDATE
        assertTrue(idEventTest > 0, "❌ ID invalide");

        List<Event> events = eventServices.afficherEvent();
        Event event = events.stream()
                .filter(e -> e.getIdEvent() == idEventTest)
                .findFirst()
                .orElse(null);
        assertNotNull(event, "❌ Événement à modifier non trouvé");

        event.setTitre("Test Event Modifié");
        event.setTypeEvent("Conférence");
        event.setMaxParticipant(30);

        eventServices.modifierEvent(event);
        System.out.println("✅ Événement modifié");
    }

    @Test
    @Order(5)
    void testVerifierModification() {
        // VERIFY UPDATE
        List<Event> events = eventServices.afficherEvent();
        Event event = events.stream()
                .filter(e -> e.getIdEvent() == idEventTest)
                .findFirst()
                .orElse(null);

        assertNotNull(event);
        assertEquals("Test Event Modifié", event.getTitre());
        assertEquals("Conférence", event.getTypeEvent());
        assertEquals(30, event.getMaxParticipant());
        System.out.println("✅ Modification vérifiée");
    }

    @Test
    @Order(6)
    void testSupprimerEvent() {
        // DELETE
        assertTrue(idEventTest > 0, "❌ ID invalide");

        eventServices.supprimerEvent(idEventTest);

        List<Event> events = eventServices.afficherEvent();
        boolean exists = events.stream()
                .anyMatch(e -> e.getIdEvent() == idEventTest);

        assertFalse(exists, "❌ L'événement devrait être supprimé");
        System.out.println("✅ Événement supprimé");
    }

    @AfterEach
    void cleanUp() {
        System.out.println("🧹 Nettoyage après test...");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("✅ Tests EventServices terminés");
    }
}