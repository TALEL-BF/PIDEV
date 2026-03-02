package Services;

import Entites.Suivie;
import Utils.Mydatabase;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SuivieServicesTest {

    private static SuivieServices service;
    private static Connection con;
    private static int createdId;

    @BeforeAll
    static void setup() throws SQLException {
        Mydatabase.getInstance();
        service = new SuivieServices();
        con = Mydatabase.getInstance().getConnection();
    }

    @Test
    @Order(1)
    void testAjouterSuivie() throws SQLException {
        String uniqueName = "TEST_ENFANT_" + System.currentTimeMillis();

        Suivie s = new Suivie(
                uniqueName,
                8,
                "TEST_PSY",
                new Timestamp(System.currentTimeMillis()),
                5, 5, 5,
                "CALME",
                "BONNE",
                "OBS_TEST",
                "EFFECTUE"
        );

        service.ajouterSuivie(s);

        // récupérer l'ID inséré (comme ton service ne renvoie pas l'id)
        String sql = "SELECT ID_SUIVIE FROM suivie WHERE NOM_ENFANT=? ORDER BY ID_SUIVIE DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uniqueName);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "La ligne insérée doit exister");
                createdId = rs.getInt("ID_SUIVIE");
                assertTrue(createdId > 0);
            }
        }
    }

    @Test
    @Order(2)
    void testAfficherSuivie() {
        List<Suivie> list = service.afficherSuivie();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "La liste doit contenir au moins 1 élément");
    }

    @Test
    @Order(3)
    void testModifierSuivie() throws SQLException {
        // Charger la ligne créée
        String sql = "SELECT * FROM suivie WHERE ID_SUIVIE=?";
        Suivie s;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, createdId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());

                s = new Suivie(
                        rs.getInt("ID_SUIVIE"),
                        rs.getString("NOM_ENFANT"),
                        rs.getInt("AGE"),
                        rs.getString("NOM_PSY"),
                        rs.getTimestamp("DATE_SUIVIE"),
                        rs.getInt("SCORE_HUMEUR"),
                        rs.getInt("SCORE_STRESS"),
                        rs.getInt("SCORE_ATTENTION"),
                        rs.getString("COMPORTEMENT"),
                        rs.getString("INTERACTION_SOCIALE"),
                        rs.getString("OBSERVATION"),
                        rs.getString("STATUT")
                );
            }
        }

        s.setObservation("OBS_MODIF_TEST");
        s.setScoreStress(2);

        service.modifierSuivie(s);

        // vérifier en base
        try (PreparedStatement ps = con.prepareStatement("SELECT OBSERVATION, SCORE_STRESS FROM suivie WHERE ID_SUIVIE=?")) {
            ps.setInt(1, createdId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("OBS_MODIF_TEST", rs.getString("OBSERVATION"));
                assertEquals(2, rs.getInt("SCORE_STRESS"));
            }
        }
    }

    @Test
    @Order(4)
    void testSupprimerSuivie() throws SQLException {
        service.supprimerSuivie(createdId);

        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) c FROM suivie WHERE ID_SUIVIE=?")) {
            ps.setInt(1, createdId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt("c"), "La ligne doit être supprimée");
            }
        }
    }
}