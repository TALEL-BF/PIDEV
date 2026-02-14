package Services;

import Entites.Seance;
import IServices.ISeanceServices;
import Utils.Mydatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeanceServices implements ISeanceServices {

    Connection con;

    public SeanceServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouterSeance(Seance seance) {
        String req = "INSERT INTO seance(titre_seance, description, date_seance, jours_semaine, duree, statut_seance, id_autiste, id_professeur, id_cours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, seance.getTitreSeance());
            ps.setString(2, seance.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(seance.getDateSeance()));
            ps.setString(4, seance.getJoursSemaine());
            ps.setInt(5, seance.getDuree());
            ps.setString(6, seance.getStatutSeance());
            ps.setInt(7, seance.getIdAutiste());
            ps.setInt(8, seance.getIdProfesseur());
            ps.setInt(9, seance.getIdCours());

            ps.executeUpdate();
            System.out.println("Séance ajoutée avec succès");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerSeance(int id) {
        String req = "DELETE FROM seance WHERE id_seance = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Séance avec id = " + id + " est supprimée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierSeance(Seance seance) {
        String req = "UPDATE seance SET titre_seance = ?, description = ?, date_seance = ?, jours_semaine = ?, duree = ?, statut_seance = ?, id_autiste = ?, id_professeur = ?, id_cours = ? WHERE id_seance = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, seance.getTitreSeance());
            ps.setString(2, seance.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(seance.getDateSeance()));
            ps.setString(4, seance.getJoursSemaine());
            ps.setInt(5, seance.getDuree());
            ps.setString(6, seance.getStatutSeance());
            ps.setInt(7, seance.getIdAutiste());
            ps.setInt(8, seance.getIdProfesseur());
            ps.setInt(9, seance.getIdCours());
            ps.setInt(10, seance.getIdSeance());

            ps.executeUpdate();
            System.out.println("Séance avec id = " + seance.getIdSeance() + " est modifiée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Seance> afficherSeances() {
        List<Seance> seances = new ArrayList<>();
        String req = "SELECT * FROM seance";
        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Seance seance = new Seance(
                    rs.getInt("id_seance"),
                    rs.getString("titre_seance"),
                    rs.getString("description"),
                    rs.getTimestamp("date_seance").toLocalDateTime(),
                    rs.getString("jours_semaine"),
                    rs.getInt("duree"),
                    rs.getString("statut_seance"),
                    rs.getInt("id_autiste"),
                    rs.getInt("id_professeur"),
                    rs.getInt("id_cours")
                );
                seances.add(seance);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return seances;
    }

    @Override
    public Seance getSeanceById(int id) {
        String req = "SELECT * FROM seance WHERE id_seance = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Seance(
                    rs.getInt("id_seance"),
                    rs.getString("titre_seance"),
                    rs.getString("description"),
                    rs.getTimestamp("date_seance").toLocalDateTime(),
                    rs.getString("jours_semaine"),
                    rs.getInt("duree"),
                    rs.getString("statut_seance"),
                    rs.getInt("id_autiste"),
                    rs.getInt("id_professeur"),
                    rs.getInt("id_cours")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Seance> afficherSeancesByStatut(String statut) {
        List<Seance> seances = new ArrayList<>();
        String req = "SELECT * FROM seance WHERE statut_seance = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Seance seance = new Seance(
                    rs.getInt("id_seance"),
                    rs.getString("titre_seance"),
                    rs.getString("description"),
                    rs.getTimestamp("date_seance").toLocalDateTime(),
                    rs.getString("jours_semaine"),
                    rs.getInt("duree"),
                    rs.getString("statut_seance"),
                    rs.getInt("id_autiste"),
                    rs.getInt("id_professeur"),
                    rs.getInt("id_cours")
                );
                seances.add(seance);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return seances;
    }
}

