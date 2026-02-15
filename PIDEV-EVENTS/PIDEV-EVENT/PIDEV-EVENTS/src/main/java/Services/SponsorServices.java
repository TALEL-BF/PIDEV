package Services;

import Entites.Event;
import Entites.Sponsor;
import IServices.ISponsorServices;
import Utils.Mydatabase;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorServices implements ISponsorServices {

    Connection con;

    public SponsorServices() {
        con = Mydatabase.getInstance().getConnection();
    }
    @Override
    public List<Sponsor> getTopSponsors() {
        List<Sponsor> sponsors = new ArrayList<>();
        String query = "SELECT idSponsor, nom, typeSponsor, email, telephone, description, image FROM sponsor LIMIT 3";

        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Sponsor sponsor = new Sponsor(
                        rs.getInt("idSponsor"),
                        rs.getString("nom"),
                        rs.getString("typeSponsor"),
                        rs.getString("email"),
                        rs.getInt("telephone"),
                        rs.getString("description"),
                        rs.getString("image")
                );
                sponsors.add(sponsor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sponsors;
    }

    // ================= ADD SPONSOR =================
    @Override
    public void ajoutSponsor(Sponsor sponsor) {
        String req = "INSERT INTO sponsor (nom, typeSponsor, email, telephone, description, image) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, sponsor.getNom());
            ps.setString(2, sponsor.getTypeSponsor());
            ps.setString(3, sponsor.getEmail());
            ps.setInt(4, sponsor.getTelephone());
            ps.setString(5, sponsor.getDescription());
            ps.setString(6, sponsor.getImage()); // ⭐ NOUVEAU CHAMP IMAGE

            ps.executeUpdate();

            // Get generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                sponsor.setIdSponsor(rs.getInt(1));
            }

            System.out.println("Sponsor ajouté avec succès, ID: " + sponsor.getIdSponsor() + ", Image: " + sponsor.getImage());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= DELETE SPONSOR =================
    @Override
    public void supprimerSponsor(int idSponsor) {
        String req = "DELETE FROM sponsor WHERE idSponsor = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idSponsor);
            ps.executeUpdate();
            System.out.println("Sponsor with id = " + idSponsor + " deleted");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= UPDATE SPONSOR =================
    @Override
    public void modifierSponsor(Sponsor sponsor) {
        String req = "UPDATE sponsor SET nom=?, typeSponsor=?, email=?, telephone=?, description=?, image=? WHERE idSponsor=?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, sponsor.getNom());
            ps.setString(2, sponsor.getTypeSponsor());
            ps.setString(3, sponsor.getEmail());
            ps.setInt(4, sponsor.getTelephone());
            ps.setString(5, sponsor.getDescription());
            ps.setString(6, sponsor.getImage()); // ⭐ NOUVEAU CHAMP IMAGE
            ps.setInt(7, sponsor.getIdSponsor());

            ps.executeUpdate();
            System.out.println("Sponsor updated successfully, Image: " + sponsor.getImage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= DISPLAY ALL SPONSORS =================
    @Override
    public List<Sponsor> afficherSponsor() {
        List<Sponsor> sponsors = new ArrayList<>();
        String req = "SELECT * FROM sponsor";

        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);

            while (rs.next()) {
                Sponsor sponsor = new Sponsor(
                        rs.getInt("idSponsor"),
                        rs.getString("nom"),
                        rs.getString("typeSponsor"),
                        rs.getString("email"),
                        rs.getInt("telephone"),
                        rs.getString("description"),
                        rs.getString("image") // ⭐ NOUVEAU CHAMP IMAGE
                );
                sponsors.add(sponsor);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return sponsors;
    }

    // ================= GET SPONSOR BY ID =================
    @Override
    public Sponsor getSponsorById(int idSponsor) {
        String req = "SELECT * FROM sponsor WHERE idSponsor = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idSponsor);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Sponsor(
                        rs.getInt("idSponsor"),
                        rs.getString("nom"),
                        rs.getString("typeSponsor"),
                        rs.getString("email"),
                        rs.getInt("telephone"),
                        rs.getString("description"),
                        rs.getString("image") // ⭐ NOUVEAU CHAMP IMAGE
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // ================= SEARCH SPONSORS =================
    @Override
    public List<Sponsor> rechercherSponsor(String keyword) {
        List<Sponsor> sponsors = new ArrayList<>();
        String req = "SELECT * FROM sponsor WHERE nom LIKE ? OR typeSponsor LIKE ? OR email LIKE ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Sponsor sponsor = new Sponsor(
                        rs.getInt("idSponsor"),
                        rs.getString("nom"),
                        rs.getString("typeSponsor"),
                        rs.getString("email"),
                        rs.getInt("telephone"),
                        rs.getString("description"),
                        rs.getString("image") // ⭐ NOUVEAU CHAMP IMAGE
                );
                sponsors.add(sponsor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sponsors;
    }
    public List<Event> getEventsForSponsor(int sponsorId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.* FROM event e " +
                "JOIN event_sponsor es ON e.idEvent = es.idEvent " +  // ✅ idEvent (correct)
                "WHERE es.idSponsor = ?";                              // ✅ idSponsor (correct)

        System.out.println("🔍 SQL pour sponsor " + sponsorId + ": " + sql);

        try {
            Connection conn = Mydatabase.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sponsorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Event event = new Event();
                event.setIdEvent(rs.getInt("idEvent"));
                event.setTitre(rs.getString("titre"));
                event.setDescription(rs.getString("description"));
                event.setTypeEvent(rs.getString("typeEvent"));
                event.setLieu(rs.getString("lieu"));
                event.setMaxParticipant(rs.getInt("maxParticipant"));
                event.setDateDebut(rs.getDate("dateDebut"));
                event.setDateFin(rs.getDate("dateFin"));
                event.setHeureDebut(rs.getTime("heureDebut"));
                event.setHeureFin(rs.getTime("heureFin"));
                event.setImage(rs.getString("image"));

                events.add(event);
                System.out.println("✅ Événement trouvé: " + event.getTitre());
            }
            System.out.println("📊 Total événements trouvés: " + events.size());

        } catch (SQLException e) {
            System.out.println("❌ ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }


}