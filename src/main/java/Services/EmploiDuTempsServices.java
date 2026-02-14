package Services;

import Entites.EmploiDuTemps;
import IServices.IEmploiDuTempsServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmploiDuTempsServices implements IEmploiDuTempsServices {

    Connection con;

    public EmploiDuTempsServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouterEmploi(EmploiDuTemps emploi) {
        String req = "INSERT INTO emploi_du_temps(annee_scolaire, jour_semaine, tranche_horaire, id_rdv, id_seance) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, emploi.getAnneeScolaire());
            ps.setString(2, emploi.getJourSemaine());
            ps.setString(3, emploi.getTrancheHoraire());

            if (emploi.getIdRdv() != null) {
                ps.setInt(4, emploi.getIdRdv());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (emploi.getIdSeance() != null) {
                ps.setInt(5, emploi.getIdSeance());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.executeUpdate();
            System.out.println("Emploi du temps ajouté avec succès");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerEmploi(int id) {
        String req = "DELETE FROM emploi_du_temps WHERE id_emploi = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Emploi du temps avec id = " + id + " est supprimé");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierEmploi(EmploiDuTemps emploi) {
        String req = "UPDATE emploi_du_temps SET annee_scolaire = ?, jour_semaine = ?, tranche_horaire = ?, id_rdv = ?, id_seance = ? WHERE id_emploi = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, emploi.getAnneeScolaire());
            ps.setString(2, emploi.getJourSemaine());
            ps.setString(3, emploi.getTrancheHoraire());

            if (emploi.getIdRdv() != null) {
                ps.setInt(4, emploi.getIdRdv());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (emploi.getIdSeance() != null) {
                ps.setInt(5, emploi.getIdSeance());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(6, emploi.getIdEmploi());

            ps.executeUpdate();
            System.out.println("Emploi du temps avec id = " + emploi.getIdEmploi() + " est modifié");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<EmploiDuTemps> afficherEmplois() {
        List<EmploiDuTemps> emplois = new ArrayList<>();
        String req = "SELECT * FROM emploi_du_temps";
        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                EmploiDuTemps emploi = new EmploiDuTemps(
                    rs.getInt("id_emploi"),
                    rs.getString("annee_scolaire"),
                    rs.getString("jour_semaine"),
                    rs.getString("tranche_horaire"),
                    (Integer) rs.getObject("id_rdv"),
                    (Integer) rs.getObject("id_seance")
                );
                emplois.add(emploi);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return emplois;
    }

    @Override
    public EmploiDuTemps getEmploiById(int id) {
        String req = "SELECT * FROM emploi_du_temps WHERE id_emploi = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new EmploiDuTemps(
                    rs.getInt("id_emploi"),
                    rs.getString("annee_scolaire"),
                    rs.getString("jour_semaine"),
                    rs.getString("tranche_horaire"),
                    (Integer) rs.getObject("id_rdv"),
                    (Integer) rs.getObject("id_seance")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<EmploiDuTemps> afficherEmploisByJour(String jour) {
        List<EmploiDuTemps> emplois = new ArrayList<>();
        String req = "SELECT * FROM emploi_du_temps WHERE jour_semaine = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, jour);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EmploiDuTemps emploi = new EmploiDuTemps(
                    rs.getInt("id_emploi"),
                    rs.getString("annee_scolaire"),
                    rs.getString("jour_semaine"),
                    rs.getString("tranche_horaire"),
                    (Integer) rs.getObject("id_rdv"),
                    (Integer) rs.getObject("id_seance")
                );
                emplois.add(emploi);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return emplois;
    }
}

