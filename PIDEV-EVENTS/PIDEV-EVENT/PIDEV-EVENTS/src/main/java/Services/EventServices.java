package Services;

import Entites.Event;
  // ⭐ ADD THIS IMPORT!
import Entites.Sponsor;
import IServices.IEventServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventServices implements IEventServices {

    Connection con;

    public EventServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    // ================= ADD EVENT =================
    @Override
    public void ajoutEvent(Event event) {
        String req = "INSERT INTO event (titre, description, typeEvent, lieu, maxParticipant, dateDebut, dateFin, heureDebut, heureFin, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getTitre());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getTypeEvent());
            ps.setString(4, event.getLieu());
            ps.setInt(5, event.getMaxParticipant());
            ps.setDate(6, event.getDateDebut());
            ps.setDate(7, event.getDateFin());
            ps.setTime(8, event.getHeureDebut());
            ps.setTime(9, event.getHeureFin());
            ps.setString(10, event.getImage());

            ps.executeUpdate();

            // Get generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                event.setIdEvent(rs.getInt(1));
            }

            System.out.println("Event ajouté avec succès, ID: " + event.getIdEvent());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= DELETE EVENT =================
    @Override
    public void supprimerEvent(int idEvent) {
        String req = "DELETE FROM event WHERE idEvent = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idEvent);
            ps.executeUpdate();
            System.out.println("Event with id = " + idEvent + " deleted");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= UPDATE EVENT =================
    @Override
    public void modifierEvent(Event event) {
        String req = "UPDATE event SET titre=?, description=?, typeEvent=?, lieu=?, maxParticipant=?, dateDebut=?, dateFin=?, heureDebut=?, heureFin=?, image=? WHERE idEvent=?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, event.getTitre());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getTypeEvent());
            ps.setString(4, event.getLieu());
            ps.setInt(5, event.getMaxParticipant());
            ps.setDate(6, event.getDateDebut());
            ps.setDate(7, event.getDateFin());
            ps.setTime(8, event.getHeureDebut());
            ps.setTime(9, event.getHeureFin());
            ps.setString(10, event.getImage());
            ps.setInt(11, event.getIdEvent());

            ps.executeUpdate();
            System.out.println("Event updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= DISPLAY ALL EVENTS =================
    @Override
    public List<Event> afficherEvent() {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM event";

        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);

            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("idEvent"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("typeEvent"),
                        rs.getString("lieu"),
                        rs.getInt("maxParticipant"),
                        rs.getDate("dateDebut"),
                        rs.getDate("dateFin"),
                        rs.getTime("heureDebut"),
                        rs.getTime("heureFin"),
                        rs.getString("image")
                );
                events.add(event);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return events;
    }
    // ================= GET SPONSORS FOR EVENT =================
    @Override
    public List<Sponsor> getSponsorsForEvent(int idEvent) {
        List<Sponsor> sponsors = new ArrayList<>();
        String req = "SELECT s.* FROM sponsor s " +
                "JOIN event_sponsor es ON s.idSponsor = es.idSponsor " +
                "WHERE es.idEvent = ?";

        System.out.println("🔍 SQL EXÉCUTÉE: " + req);
        System.out.println("🔍 Paramètre idEvent = " + idEvent);

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idEvent);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Sponsor s = new Sponsor(
                        rs.getInt("idSponsor"),
                        rs.getString("nom"),
                        rs.getString("typeSponsor"),
                        rs.getString("email"),
                        rs.getInt("telephone"),
                        rs.getString("description"),
                        rs.getString("image")
                );
                sponsors.add(s);
                System.out.println("✅ Sponsor trouvé: " + s.getNom());
            }
            System.out.println("📊 Total sponsors trouvés: " + sponsors.size());

        } catch (SQLException e) {
            System.out.println("❌ ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return sponsors;
    }
    // ================= ADD SPONSOR TO EVENT =================
    @Override
    public void addSponsorToEvent(int idEvent, int idSponsor) {
        String req = "INSERT INTO event_sponsor (idEvent, idSponsor) VALUES (?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idEvent);
            ps.setInt(2, idSponsor);
            ps.executeUpdate();
            System.out.println("✅ Sponsor " + idSponsor + " lié à l'événement " + idEvent);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= REMOVE ALL SPONSORS FROM EVENT =================
    @Override
    public void removeAllSponsorsFromEvent(int idEvent) {
        String req = "DELETE FROM event_sponsor WHERE idEvent = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idEvent);
            ps.executeUpdate();
            System.out.println("✅ Tous les sponsors retirés de l'événement " + idEvent);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= GET SPONSORS FOR EVENT =================

}