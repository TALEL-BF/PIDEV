package Services;

import Entites.Therapie;
import IServices.ITherapieServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TherapieServices implements ITherapieServices {

    Connection con;

    public TherapieServices() {
        con = Mydatabase.getInstance().getConnection();
        try {
            con.setCatalog("pidev"); // ✅ force la DB courante
            System.out.println("DB USED = " + con.getCatalog());
            System.out.println("URL = " + con.getMetaData().getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void ajouterTherapie(Therapie t) {
        String req = "INSERT INTO therapie (" +
                "NOM_EXERCICE, TYPE_EXERCICE, OBJECTIF, DESCRIPTION, DUREE_MIN, " +
                "MATERIEL, ADAPTATION_TSA, CIBLE, " +
                "NIVEAUX_HUMEUR, NIVEAUX_ATTENTION, NIVEAUX_STRESSE, " +
                "COMPORTEMENT, INTERACTION, NIVEAU" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


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
            ps.setString(11, emptyToNull(t.getNiveauxStresse())); // ✅ correspond à NIVEAUX_STRESSE en DB

            ps.setString(12, emptyToNull(t.getComportement()));
            ps.setString(13, emptyToNull(t.getInteraction()));

            if (t.getNiveau() == null) ps.setNull(14, Types.INTEGER);
            else ps.setInt(14, t.getNiveau());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur INSERT therapie: " + e.getMessage(), e);
        }
    }


    @Override
    public void modifierTherapie(Therapie t) {
        String req = "UPDATE therapie SET " +
                "NOM_EXERCICE=?, TYPE_EXERCICE=?, OBJECTIF=?, DESCRIPTION=?, DUREE_MIN=?, " +
                "MATERIEL=?, ADAPTATION_TSA=?, CIBLE=?, " +
                "NIVEAUX_HUMEUR=?, NIVEAUX_ATTENTION=?, NIVEAUX_STRESSE=?, " +
                "COMPORTEMENT=?, INTERACTION=?, NIVEAU=? " +
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

            if (t.getNiveau() == null) ps.setNull(14, Types.INTEGER);
            else ps.setInt(14, t.getNiveau());

            ps.setInt(15, t.getIdTherapie());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur UPDATE therapie: " + e.getMessage(), e);
        }
    }

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

    @Override
    public List<Therapie> afficherTherapie() {
        List<Therapie> list = new ArrayList<>();
        String req = "SELECT * FROM pidev.therapie";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            // build set of returned columns
            ResultSetMetaData md = rs.getMetaData();
            java.util.Set<String> cols = new java.util.HashSet<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                cols.add(md.getColumnLabel(i).toUpperCase());
            }

            while (rs.next()) {
                Integer niveau = (Integer) (cols.contains("NIVEAU") ? rs.getObject("NIVEAU") : null);

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




    private String emptyToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }
}
