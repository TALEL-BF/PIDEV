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

    @Override
    public List<String> listerNomsEnfants() {
        List<String> noms = new ArrayList<>();
        String sql = "SELECT DISTINCT NOM_ENFANT FROM suivie ORDER BY NOM_ENFANT";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                noms.add(rs.getString("NOM_ENFANT"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return noms;
    }

    @Override
    public List<Suivie> statsParEnfant(String nomEnfant) {
        List<Suivie> list = new ArrayList<>();
        String sql = "SELECT DATE_SUIVIE, SCORE_HUMEUR, SCORE_STRESS, SCORE_ATTENTION " +
                "FROM suivie WHERE NOM_ENFANT=? ORDER BY DATE_SUIVIE ASC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nomEnfant);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Suivie s = new Suivie();
                    s.setDateSuivie(rs.getTimestamp("DATE_SUIVIE"));
                    s.setScoreHumeur(rs.getInt("SCORE_HUMEUR"));
                    s.setScoreStress(rs.getInt("SCORE_STRESS"));
                    s.setScoreAttention(rs.getInt("SCORE_ATTENTION"));
                    list.add(s);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    public List<String> getEnfantsByEmail(String emailParent) {
        List<String> noms = new ArrayList<>();
        String sql = "SELECT DISTINCT NOM_ENFANT FROM suivie WHERE EMAIL_PARENT=? ORDER BY NOM_ENFANT";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailParent);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    noms.add(rs.getString("NOM_ENFANT"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return noms;
    }
    public List<Suivie> getStatsByEmailAndEnfant(String email, String enfant) {
        List<Suivie> list = new ArrayList<>();

        String sql = "SELECT DATE_SUIVIE, SCORE_HUMEUR, SCORE_STRESS, SCORE_ATTENTION " +
                "FROM suivie WHERE EMAIL_PARENT=? AND NOM_ENFANT=? " +
                "ORDER BY DATE_SUIVIE ASC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, enfant);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Suivie s = new Suivie();
                    s.setDateSuivie(rs.getTimestamp("DATE_SUIVIE"));
                    s.setScoreHumeur(rs.getInt("SCORE_HUMEUR"));
                    s.setScoreStress(rs.getInt("SCORE_STRESS"));
                    s.setScoreAttention(rs.getInt("SCORE_ATTENTION"));
                    list.add(s);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
    public Suivie getDernierSuivi(String email, String enfant) {
        String sql = "SELECT * FROM suivie " +
                "WHERE EMAIL_PARENT=? AND NOM_ENFANT=? " +
                "ORDER BY DATE_SUIVIE DESC, ID_SUIVIE DESC " +
                "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, enfant);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
                    return s;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public void updateConseilIA(int idSuivie, String crResume) {
        String sql = "UPDATE suivie SET CR_RESUME = ? WHERE ID_SUIVIE = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, crResume);
            ps.setInt(2, idSuivie);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update CR_RESUME (ID_SUIVIE=" + idSuivie + ")", e);
        }
    }
    public void updateParentUpload(int idSuivie, String pdfName, String pdfPath, String subject) throws Exception {
        String sql = """
        UPDATE suivie
        SET PARENT_PDF_NAME = ?,
            PARENT_PDF_PATH = ?,
            PARENT_PDF_SUBJECT = ?,
            PARENT_PDF_UPLOADED_AT = CURRENT_TIMESTAMP,
            PARENT_PDF_SEEN = 0
        WHERE ID_SUIVIE = ?
    """;

        try (java.sql.Connection con = Utils.Mydatabase.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pdfName);
            ps.setString(2, pdfPath);
            ps.setString(3, subject);
            ps.setInt(4, idSuivie);
            ps.executeUpdate();
        }
    }
    public int countNewParentUploads() throws Exception {
        String sql = "SELECT COUNT(*) FROM suivie WHERE PARENT_PDF_PATH IS NOT NULL AND PARENT_PDF_SEEN = 0";
        try (var con = Utils.Mydatabase.getInstance().getConnection();
             var st = con.prepareStatement(sql);
             var rs = st.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public java.util.List<ParentUploadNotif> listParentUploads() throws Exception {
        String sql = """
        SELECT ID_SUIVIE, EMAIL_PARENT, NOM_ENFANT, PARENT_PDF_SUBJECT, PARENT_PDF_UPLOADED_AT,
               PARENT_PDF_NAME, PARENT_PDF_PATH, PARENT_PDF_SEEN
        FROM suivie
        WHERE PARENT_PDF_PATH IS NOT NULL
        ORDER BY PARENT_PDF_UPLOADED_AT DESC
    """;

        java.util.List<ParentUploadNotif> out = new java.util.ArrayList<>();
        try (var con = Utils.Mydatabase.getInstance().getConnection();
             var st = con.prepareStatement(sql);
             var rs = st.executeQuery()) {

            while (rs.next()) {
                ParentUploadNotif n = new ParentUploadNotif();
                n.idSuivie = rs.getInt("ID_SUIVIE");
                n.email = rs.getString("EMAIL_PARENT");
                n.enfant = rs.getString("NOM_ENFANT");
                n.subject = rs.getString("PARENT_PDF_SUBJECT");
                n.uploadedAt = rs.getTimestamp("PARENT_PDF_UPLOADED_AT");
                n.fileName = rs.getString("PARENT_PDF_NAME");
                n.filePath = rs.getString("PARENT_PDF_PATH");
                n.seen = rs.getInt("PARENT_PDF_SEEN") == 1;
                out.add(n);
            }
        }
        return out;
    }

    public void markParentUploadSeen(int idSuivie) throws Exception {
        String sql = "UPDATE suivie SET PARENT_PDF_SEEN = 1 WHERE ID_SUIVIE = ?";
        try (var con = Utils.Mydatabase.getInstance().getConnection();
             var ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSuivie);
            ps.executeUpdate();
        }
    }

    public static class ParentUploadNotif {
        public int idSuivie;
        public String email;
        public String enfant;
        public String subject;
        public java.sql.Timestamp uploadedAt;
        public String fileName;
        public String filePath;
        public boolean seen;
    }

}