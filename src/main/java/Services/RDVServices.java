package Services;

import Entites.RDV;
import IServices.IRDVServices;
import Utils.Mydatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RDVServices implements IRDVServices {

    Connection con;

    public RDVServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouterRDV(RDV rdv) {
        String req = "INSERT INTO rdv(type_consultation, date_heure_rdv, statut_rdv, duree_rdv_minutes, id_psychologue, id_autiste) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, rdv.getTypeConsultation());
            ps.setTimestamp(2, Timestamp.valueOf(rdv.getDateHeureRdv()));
            ps.setString(3, rdv.getStatutRdv());
            ps.setInt(4, rdv.getDureeRdvMinutes());
            ps.setInt(5, rdv.getIdPsychologue());
            ps.setInt(6, rdv.getIdAutiste());

            ps.executeUpdate();
            System.out.println("RDV ajouté avec succès");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerRDV(int id) {
        String req = "DELETE FROM rdv WHERE id_rdv = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("RDV avec id = " + id + " est supprimé");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierRDV(RDV rdv) {
        String req = "UPDATE rdv SET type_consultation = ?, date_heure_rdv = ?, statut_rdv = ?, duree_rdv_minutes = ?, id_psychologue = ?, id_autiste = ? WHERE id_rdv = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, rdv.getTypeConsultation());
            ps.setTimestamp(2, Timestamp.valueOf(rdv.getDateHeureRdv()));
            ps.setString(3, rdv.getStatutRdv());
            ps.setInt(4, rdv.getDureeRdvMinutes());
            ps.setInt(5, rdv.getIdPsychologue());
            ps.setInt(6, rdv.getIdAutiste());
            ps.setInt(7, rdv.getIdRdv());

            ps.executeUpdate();
            System.out.println("RDV avec id = " + rdv.getIdRdv() + " est modifié");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RDV> afficherRDV() {
        List<RDV> rdvs = new ArrayList<>();
        String req = "SELECT * FROM rdv";
        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                RDV rdv = new RDV(
                    rs.getInt("id_rdv"),
                    rs.getString("type_consultation"),
                    rs.getTimestamp("date_heure_rdv").toLocalDateTime(),
                    rs.getString("statut_rdv"),
                    rs.getInt("duree_rdv_minutes"),
                    rs.getInt("id_psychologue"),
                    rs.getInt("id_autiste")
                );
                rdvs.add(rdv);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rdvs;
    }

    @Override
    public RDV getRDVById(int id) {
        String req = "SELECT * FROM rdv WHERE id_rdv = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new RDV(
                    rs.getInt("id_rdv"),
                    rs.getString("type_consultation"),
                    rs.getTimestamp("date_heure_rdv").toLocalDateTime(),
                    rs.getString("statut_rdv"),
                    rs.getInt("duree_rdv_minutes"),
                    rs.getInt("id_psychologue"),
                    rs.getInt("id_autiste")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<RDV> afficherRDVByStatut(String statut) {
        List<RDV> rdvs = new ArrayList<>();
        String req = "SELECT * FROM rdv WHERE statut_rdv = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RDV rdv = new RDV(
                    rs.getInt("id_rdv"),
                    rs.getString("type_consultation"),
                    rs.getTimestamp("date_heure_rdv").toLocalDateTime(),
                    rs.getString("statut_rdv"),
                    rs.getInt("duree_rdv_minutes"),
                    rs.getInt("id_psychologue"),
                    rs.getInt("id_autiste")
                );
                rdvs.add(rdv);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rdvs;
    }
}
