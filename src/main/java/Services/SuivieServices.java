package Services;

import Entites.Suivie;
import IServices.ISuivieServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuivieServices implements ISuivieServices {

    Connection con;

    public SuivieServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouterSuivie(Suivie s) {
        String req = "INSERT INTO suivie (NOM_ENFANT, EMAIL_PARENT, AGE, NOM_PSY, DATE_SUIVIE, " +
                "SCORE_HUMEUR, SCORE_STRESS, SCORE_ATTENTION, NIVEAU_SEANCE, " +
                "COMPORTEMENT, INTERACTION_SOCIALE, OBSERVATION, STATUT, " +
                "ID_THERAPIE_RECO, CR_RESUME, CR_PDF_PATH) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, s.getNomEnfant());
            ps.setString(2, s.getEmailParent());
            ps.setInt(3, s.getAge());
            ps.setString(4, s.getNomPsy());
            ps.setTimestamp(5, s.getDateSuivie());

            ps.setInt(6, s.getScoreHumeur());
            ps.setInt(7, s.getScoreStress());
            ps.setInt(8, s.getScoreAttention());

            if (s.getNiveauSeance() == null) ps.setNull(9, Types.INTEGER);
            else ps.setInt(9, s.getNiveauSeance());

            ps.setString(10, s.getComportement());
            ps.setString(11, s.getInteractionSociale());
            ps.setString(12, s.getObservation());
            ps.setString(13, s.getStatut());

            if (s.getIdTherapieReco() == null) ps.setNull(14, Types.INTEGER);
            else ps.setInt(14, s.getIdTherapieReco());

            ps.setString(15, s.getCrResume());
            ps.setString(16, s.getCrPdfPath());

            ps.executeUpdate();
            System.out.println("Suivie ajouté avec succès");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerSuivie(int idSuivie) {
        String req = "DELETE FROM suivie WHERE ID_SUIVIE = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idSuivie);
            ps.executeUpdate();
            System.out.println("Suivie supprimé : ID_SUIVIE = " + idSuivie);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierSuivie(Suivie s) {
        String req = "UPDATE suivie SET NOM_ENFANT=?, EMAIL_PARENT=?, AGE=?, NOM_PSY=?, " +
                "SCORE_HUMEUR=?, SCORE_STRESS=?, SCORE_ATTENTION=?, NIVEAU_SEANCE=?, " +
                "COMPORTEMENT=?, INTERACTION_SOCIALE=?, OBSERVATION=?, STATUT=?, " +
                "ID_THERAPIE_RECO=?, CR_RESUME=?, CR_PDF_PATH=? " +
                "WHERE ID_SUIVIE=?";


        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, s.getNomEnfant());
            ps.setString(2, s.getEmailParent());
            ps.setInt(3, s.getAge());
            ps.setString(4, s.getNomPsy());

            ps.setInt(5, s.getScoreHumeur());
            ps.setInt(6, s.getScoreStress());
            ps.setInt(7, s.getScoreAttention());

            if (s.getNiveauSeance() == null) ps.setNull(8, Types.INTEGER);
            else ps.setInt(8, s.getNiveauSeance());

            ps.setString(9, s.getComportement());
            ps.setString(10, s.getInteractionSociale());
            ps.setString(11, s.getObservation());
            ps.setString(12, s.getStatut());

            if (s.getIdTherapieReco() == null) ps.setNull(13, Types.INTEGER);
            else ps.setInt(13, s.getIdTherapieReco());

            ps.setString(14, s.getCrResume());
            ps.setString(15, s.getCrPdfPath());

            ps.setInt(16, s.getIdSuivie());


            ps.executeUpdate();
            System.out.println("Suivie modifié : ID_SUIVIE = " + s.getIdSuivie());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Suivie> afficherSuivie() {
        List<Suivie> suivies = new ArrayList<>();
        String req = "SELECT * FROM suivie";

        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);

            while (rs.next()) {
                Suivie s = new Suivie(
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
                        rs.getString("STATUT"),
                        rs.getString("EMAIL_PARENT"),
                        (Integer) rs.getObject("NIVEAU_SEANCE"),
                        (Integer) rs.getObject("ID_THERAPIE_RECO"),
                        rs.getString("CR_RESUME"),
                        rs.getString("CR_PDF_PATH")
                );


// ✅ nouveaux champs (simple, après construction)
                s.setEmailParent(rs.getString("EMAIL_PARENT"));

                int n = rs.getInt("NIVEAU_SEANCE");
                s.setNiveauSeance(rs.wasNull() ? null : n);

                int idt = rs.getInt("ID_THERAPIE_RECO");
                s.setIdTherapieReco(rs.wasNull() ? null : idt);

                s.setCrResume(rs.getString("CR_RESUME"));
                s.setCrPdfPath(rs.getString("CR_PDF_PATH"));

                suivies.add(s);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return suivies;
    }
}