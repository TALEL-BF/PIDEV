package Services;

import Entites.Therapie;
import IServices.ITherapieServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.*;

public class TherapieServices implements ITherapieServices {

    private final Connection con;

    public TherapieServices() {
        con = Mydatabase.getInstance().getConnection();
        try {
            con.setCatalog("pidev"); // force DB courante
            System.out.println("DB USED = " + con.getCatalog());
            System.out.println("URL = " + con.getMetaData().getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // CREATE
    // =========================
    @Override
    public void ajouterTherapie(Therapie t) {
        String sql =
                "INSERT INTO therapie (" +
                        "NOM_EXERCICE, TYPE_EXERCICE, OBJECTIF, DESCRIPTION, DUREE_MIN, " +
                        "MATERIEL, ADAPTATION_TSA, CIBLE, " +
                        "NIVEAUX_HUMEUR, NIVEAUX_ATTENTION, NIVEAUX_STRESSE, " +
                        "COMPORTEMENT, INTERACTION, NIVEAU" +
                        ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; // ✅ 14 ? (pas 16)

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getNomExercice());
            ps.setString(2, t.getTypeExercice());
            ps.setString(3, t.getObjectif());
            ps.setString(4, t.getDescription());
            ps.setInt(5, t.getDureeMin());

            ps.setString(6, emptyToNull(t.getMateriel()));
            ps.setString(7, emptyToNull(t.getAdaptationTsa()));
            ps.setString(8, emptyToNull(t.getCible()));

            ps.setString(9,  emptyToNull(t.getNiveauxHumeur()));
            ps.setString(10, emptyToNull(t.getNiveauxAttention()));
            ps.setString(11, emptyToNull(t.getNiveauxStresse()));

            ps.setString(12, emptyToNull(t.getComportement()));
            ps.setString(13, emptyToNull(t.getInteraction()));

            if (t.getNiveau() == null) ps.setNull(14, Types.INTEGER);
            else ps.setInt(14, t.getNiveau());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur INSERT therapie: " + e.getMessage(), e);
        }
    }



    // =========================
    // UPDATE
    // =========================
    @Override
    public void modifierTherapie(Therapie t) {
        String req =
                "UPDATE therapie SET " +
                        "NOM_EXERCICE=?, TYPE_EXERCICE=?, OBJECTIF=?, DESCRIPTION=?, DUREE_MIN=?, " +
                        "MATERIEL=?, ADAPTATION_TSA=?, CIBLE=?, " +
                        "NIVEAUX_HUMEUR=?, NIVEAUX_ATTENTION=?, NIVEAUX_STRESSE=?, " +
                        "COMPORTEMENT=?, INTERACTION=?, " +
                        "NIVEAU=? " +
                        "WHERE ID_THERAPIE=?";

        try (PreparedStatement ps = con.prepareStatement(req)) {

            ps.setString(1, t.getNomExercice());
            ps.setString(2, t.getTypeExercice());
            ps.setString(3, t.getObjectif());
            ps.setString(4, t.getDescription());
            ps.setInt(5, t.getDureeMin());

            ps.setString(6, emptyToNull(t.getMateriel()));
            ps.setString(7, emptyToNull(t.getAdaptationTsa()));
            ps.setString(8, emptyToNull(t.getCible()));

            ps.setString(9,  emptyToNull(t.getNiveauxHumeur()));
            ps.setString(10, emptyToNull(t.getNiveauxAttention()));
            ps.setString(11, emptyToNull(t.getNiveauxStresse()));

            ps.setString(12, emptyToNull(t.getComportement()));
            ps.setString(13, emptyToNull(t.getInteraction()));

            // 14 = NIVEAU (selon la requête)
            if (t.getNiveau() == null) ps.setNull(14, Types.INTEGER);
            else ps.setInt(14, t.getNiveau());



            // 17 = ID_THERAPIE
            ps.setInt(15, t.getIdTherapie());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur UPDATE therapie: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETE
    // =========================
    @Override
    public void supprimerTherapie(int idTherapie) {
        String req = "DELETE FROM therapie WHERE ID_THERAPIE = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, idTherapie);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur DELETE therapie: " + e.getMessage(), e);
        }
    }

    // =========================
    // READ ALL
    // =========================
    @Override
    public List<Therapie> afficherTherapie() {
        List<Therapie> list = new ArrayList<>();
        String req = "SELECT * FROM pidev.therapie";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            ResultSetMetaData md = rs.getMetaData();
            Set<String> cols = new HashSet<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                cols.add(md.getColumnLabel(i).toUpperCase());
            }

            while (rs.next()) {
                Integer niveau = cols.contains("NIVEAU") ? (Integer) rs.getObject("NIVEAU") : null;

                Therapie t = new Therapie(
                        rs.getInt("ID_THERAPIE"),
                        rs.getString("NOM_EXERCICE"),
                        rs.getString("TYPE_EXERCICE"),
                        rs.getString("OBJECTIF"),
                        rs.getString("DESCRIPTION"),
                        rs.getInt("DUREE_MIN"),
                        cols.contains("MATERIEL") ? rs.getString("MATERIEL") : null,
                        cols.contains("ADAPTATION_TSA") ? rs.getString("ADAPTATION_TSA") : null,
                        cols.contains("CIBLE") ? rs.getString("CIBLE") : null,
                        cols.contains("NIVEAUX_HUMEUR") ? rs.getString("NIVEAUX_HUMEUR") : null,
                        cols.contains("NIVEAUX_ATTENTION") ? rs.getString("NIVEAUX_ATTENTION") : null,
                        cols.contains("NIVEAUX_STRESSE") ? rs.getString("NIVEAUX_STRESSE") : null,
                        cols.contains("COMPORTEMENT") ? rs.getString("COMPORTEMENT") : null,
                        cols.contains("INTERACTION") ? rs.getString("INTERACTION") : null,
                        niveau
                );


                list.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur SELECT therapie: " + e.getMessage(), e);
        }

        return list;
    }

    // =========================
    // READ (adaptées)
    // =========================
    public List<Therapie> chercherTherapiesAdaptees(String nivHumeur,
                                                    String nivAttention,
                                                    String nivStress) {
        List<Therapie> list = new ArrayList<>();

        String sql = "SELECT * FROM therapie " +
                "WHERE NIVEAUX_HUMEUR=? " +
                "AND NIVEAUX_ATTENTION=? " +
                "AND NIVEAUX_STRESSE=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nivHumeur);
            ps.setString(2, nivAttention);
            ps.setString(3, nivStress);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Therapie t = new Therapie(
                            rs.getInt("ID_THERAPIE"),
                            rs.getString("NOM_EXERCICE"),
                            rs.getString("TYPE_EXERCICE"),
                            rs.getString("OBJECTIF"),
                            rs.getString("DESCRIPTION"),
                            rs.getInt("DUREE_MIN"),
                            rs.getString("MATERIEL"),
                            rs.getString("ADAPTATION_TSA"),
                            rs.getString("CIBLE"),
                            rs.getString("NIVEAUX_HUMEUR"),
                            rs.getString("NIVEAUX_ATTENTION"),
                            rs.getString("NIVEAUX_STRESSE"),
                            rs.getString("COMPORTEMENT"),
                            rs.getString("INTERACTION"),
                            (Integer) rs.getObject("NIVEAU")
                    );


                    list.add(t);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur chercherTherapiesAdaptees: " + e.getMessage(), e);
        }

        return list;
    }

    // =========================
    // GET BY ID
    // =========================
    public Therapie getTherapieById(Integer id) {
        if (id == null) return null;

        String sql = "SELECT * FROM therapie WHERE ID_THERAPIE = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Therapie t = new Therapie(
                            rs.getInt("ID_THERAPIE"),
                            rs.getString("NOM_EXERCICE"),
                            rs.getString("TYPE_EXERCICE"),
                            rs.getString("OBJECTIF"),
                            rs.getString("DESCRIPTION"),
                            rs.getInt("DUREE_MIN"),
                            rs.getString("MATERIEL"),
                            rs.getString("ADAPTATION_TSA"),
                            rs.getString("CIBLE"),
                            rs.getString("NIVEAUX_HUMEUR"),
                            rs.getString("NIVEAUX_ATTENTION"),
                            rs.getString("NIVEAUX_STRESSE"),
                            rs.getString("COMPORTEMENT"),
                            rs.getString("INTERACTION"),
                            (Integer) rs.getObject("NIVEAU")
                    );


                    return t;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getTherapieById: " + e.getMessage(), e);
        }

        return null;
    }

    // =========================
    // ARTICLES ONLY
    // =========================


    // =========================
    // UTIL
    // =========================
    private String emptyToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }
}

