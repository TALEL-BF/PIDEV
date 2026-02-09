package Services;

import Entites.Event;
import IServices.IEventServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventServices implements IEventServices {

    private Connection con;

    public EventServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Event event) {
        String req = "INSERT INTO event (titre, description, typeEvent, lieu, maxParticipant, dateDebut, dateFin, heureDebut, heureFin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, event.getTitre());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getTypeEvent());
            ps.setString(4, event.getLieu());
            ps.setInt(5, event.getMaxParticipant());
            ps.setString(6, event.getDateDebut());
            ps.setString(7, event.getDateFin());
            ps.setString(8, event.getHeureDebut());
            ps.setString(9, event.getHeureFin());

            ps.executeUpdate();
            System.out.println("Event ajouté avec succès !");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean modifier(Event event) {
        String req = "UPDATE event SET titre = ?, description = ?, typeEvent = ?, lieu = ?, " +
                "maxParticipant = ?, dateDebut = ?, dateFin = ?, heureDebut = ?, heureFin = ? " +
                "WHERE idEvent = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, event.getTitre());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getTypeEvent());
            ps.setString(4, event.getLieu());
            ps.setInt(5, event.getMaxParticipant());
            ps.setString(6, event.getDateDebut());
            ps.setString(7, event.getDateFin());
            ps.setString(8, event.getHeureDebut());
            ps.setString(9, event.getHeureFin());
            ps.setInt(10, event.getIdEvent());

            ps.executeUpdate();
            System.out.println("Event avec id = " + event.getIdEvent() + " modifié !");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(int id) {
        String req = "DELETE FROM event WHERE idEvent = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Event avec id = " + id + " supprimé !");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Event> getAll() {
        List<Event> eventList = new ArrayList<>();
        String req = "SELECT * FROM event";
        try (Statement ste = con.createStatement();
             ResultSet rs = ste.executeQuery(req)) {

            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("idEvent"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("typeEvent"),
                        rs.getString("lieu"),
                        rs.getInt("maxParticipant"),
                        rs.getString("dateDebut"),
                        rs.getString("dateFin"),
                        rs.getString("heureDebut"),
                        rs.getString("heureFin")
                );
                eventList.add(event);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventList;
    }
}
