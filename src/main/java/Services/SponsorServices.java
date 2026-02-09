package Services;



import Entites.Sponsor;
import IServices.ISponsorServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorServices implements ISponsorServices<Sponsor> {

    private Connection con;

    public SponsorServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Sponsor sponsor) {
        String req = "INSERT INTO sponsor(nom, typeSponsor, email, telephone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, sponsor.getNom());
            ps.setString(2, sponsor.getTypeSponsor());
            ps.setString(3, sponsor.getEmail());
            ps.setString(4, sponsor.getTelephone());

            ps.executeUpdate();
            System.out.println("Sponsor ajouté avec succès !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean modifier(Sponsor sponsor) {
        String req = "UPDATE sponsor SET nom = ?, typeSponsor = ?, email = ?, telephone = ? WHERE idSponsor = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, sponsor.getNom());
            ps.setString(2, sponsor.getTypeSponsor());
            ps.setString(3, sponsor.getEmail());
            ps.setString(4, sponsor.getTelephone());
            ps.setInt(5, sponsor.getIdSponsor());

            ps.executeUpdate();
            System.out.println("Sponsor avec id = " + sponsor.getIdSponsor() + " modifié !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(int id) {
        String req = "DELETE FROM sponsor WHERE idSponsor = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Sponsor avec id = " + id + " supprimé !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Sponsor> getAll() {
        List<Sponsor> sponsors = new ArrayList<>();
        String req = "SELECT * FROM sponsor";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                Sponsor sponsor = new Sponsor(
                        rs.getInt("idSponsor"),
                        rs.getString("nom"),
                        rs.getString("typeSponsor"),
                        rs.getString("email"),
                        rs.getString("telephone")
                );
                sponsors.add(sponsor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sponsors;
    }
}
