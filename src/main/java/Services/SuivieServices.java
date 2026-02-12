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
        String req = "INSERT INTO suivie (NOM_ENFANT, AGE, NOM_PSY, DATE_SUIVIE, SCORE_HUMEUR, SCORE_STRESS, SCORE_ATTENTION, COMPORTEMENT, INTERACTION_SOCIALE, OBSERVATION, STATUT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, s.getNomEnfant());
            ps.setInt(2, s.getAge());
            ps.setString(3, s.getNomPsy());

            // Si tu veux laisser MySQL mettre current_timestamp(), mets null ici OU enlève DATE_SUIVIE de la requête.
            ps.setTimestamp(4, s.getDateSuivie());

            ps.setInt(5, s.getScoreHumeur());
            ps.setInt(6, s.getScoreStress());
            ps.setInt(7, s.getScoreAttention());
            ps.setString(8, s.getComportement());
            ps.setString(9, s.getInteractionSociale());
            ps.setString(10, s.getObservation());
            ps.setString(11, s.getStatut());

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
        String req = "UPDATE suivie SET NOM_ENFANT = ?, AGE = ?, NOM_PSY = ?, " +
                "SCORE_HUMEUR = ?, SCORE_STRESS = ?, SCORE_ATTENTION = ?, " +
                "COMPORTEMENT = ?, INTERACTION_SOCIALE = ?, OBSERVATION = ?, STATUT = ? " +
                "WHERE ID_SUIVIE = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, s.getNomEnfant());
            ps.setInt(2, s.getAge());
            ps.setString(3, s.getNomPsy());

            ps.setInt(4, s.getScoreHumeur());
            ps.setInt(5, s.getScoreStress());
            ps.setInt(6, s.getScoreAttention());

            ps.setString(7, s.getComportement());
            ps.setString(8, s.getInteractionSociale());
            ps.setString(9, s.getObservation());
            ps.setString(10, s.getStatut());

            ps.setInt(11, s.getIdSuivie());

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
                        rs.getString("STATUT")
                );
                suivies.add(s);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return suivies;
    }
}