package Services;

import Entites.Therapie;
import Utils.Mydatabase;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TherapieServicesTest {

    private TherapieServices service;
    private Connection con;

    private static final String TEST_PREFIX = "TEST_EX_";
    private String uniqueName;

    @BeforeEach
    void setup() throws SQLException {
        Mydatabase.getInstance();
        service = new TherapieServices();

        con = Mydatabase.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            throw new SQLException("Connexion DB fermée dans setup()");
        }
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Nettoyer les lignes test
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM therapie WHERE NOM_EXERCICE LIKE ?")) {
            ps.setString(1, TEST_PREFIX + "%");
            ps.executeUpdate();
        }

        // ❌ NE PAS fermer con ici (Singleton Mydatabase)
        // if (con != null && !con.isClosed()) con.close();
    }

    @Test
    void testAjouterTherapie() throws SQLException {
        uniqueName = TEST_PREFIX + System.currentTimeMillis();

        Therapie t = new Therapie(
                uniqueName, "EMOTION", "OBJ_TEST", "DESC_TEST",
                10, 1, "MAT_TEST", "ADAPT_TEST"
        );

        service.ajouterTherapie(t);

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) c FROM therapie WHERE NOM_EXERCICE=?")) {
            ps.setString(1, uniqueName);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("c"));
            }
        }
    }

    @Test
    void testAfficherTherapie() {
        List<Therapie> list = service.afficherTherapie();
        assertNotNull(list);
    }

    @Test
    void testModifierTherapie() throws SQLException {
        uniqueName = TEST_PREFIX + System.currentTimeMillis();

        // insert
        Therapie t = new Therapie(
                uniqueName, "EMOTION", "OBJ_TEST", "DESC_TEST",
                10, 1, "MAT_TEST", "ADAPT_TEST"
        );
        service.ajouterTherapie(t);

        // get id
        int id;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ID_THERAPIE FROM therapie WHERE NOM_EXERCICE=? ORDER BY ID_THERAPIE DESC LIMIT 1")) {
            ps.setString(1, uniqueName);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                id = rs.getInt("ID_THERAPIE");
            }
        }

        // update
        Therapie modif = new Therapie(
                id, uniqueName, "SOCIAL", "OBJ_MODIF", "DESC_MODIF",
                20, 3, "MAT_MODIF", "ADAPT_MODIF"
        );
        service.modifierTherapie(modif);

        // verify
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT TYPE_EXERCICE, NIVEAU FROM therapie WHERE ID_THERAPIE=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("SOCIAL", rs.getString("TYPE_EXERCICE"));
                assertEquals(3, rs.getInt("NIVEAU"));
            }
        }
    }

    @Test
    void testSupprimerTherapie() throws SQLException {
        uniqueName = TEST_PREFIX + System.currentTimeMillis();

        // insert
        Therapie t = new Therapie(
                uniqueName, "EMOTION", "OBJ_TEST", "DESC_TEST",
                10, 1, "MAT_TEST", "ADAPT_TEST"
        );
        service.ajouterTherapie(t);

        // get id
        int id;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ID_THERAPIE FROM therapie WHERE NOM_EXERCICE=? ORDER BY ID_THERAPIE DESC LIMIT 1")) {
            ps.setString(1, uniqueName);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                id = rs.getInt("ID_THERAPIE");
            }
        }

        // delete
        service.supprimerTherapie(id);

        // verify
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) c FROM therapie WHERE ID_THERAPIE=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt("c"));
            }
        }
    }
}