package com.auticare.services;

import com.auticare.entities.Participation;
import com.auticare.iservices.IParticipationService;
import com.auticare.utils.Mydatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService implements IParticipationService {

    Connection con;

    public ParticipationService() {
        con = Mydatabase.getInstance().getConnection();
    }

    // ================= AJOUTER PARTICIPATION =================
    @Override
    public void ajouterParticipation(Participation participation) {
        String req = "INSERT INTO participation (id_user, id_event, date_participation, nbrP) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, participation.getIdUser());
            ps.setInt(2, participation.getIdEvent());
            ps.setTimestamp(3, Timestamp.valueOf(participation.getDateParticipation()));
            ps.setInt(4, participation.getNbrP());

            ps.executeUpdate();

            // Get generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                participation.setIdParticipation(rs.getInt(1));
            }

            System.out.println("✅ Participation ajoutée avec succès, ID: " + participation.getIdParticipation() +
                    ", Places: " + participation.getNbrP());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Récupère toutes les participations pour un événement spécifique
     * (Alias pour getParticipationsByEvent)
     */
    public List<Participation> afficherParticipationsParEvent(int eventId) {
        return getParticipationsByEvent(eventId);
    }

    // ================= SUPPRIMER PARTICIPATION =================
    @Override
    public void supprimerParticipation(int idParticipation) {
        String req = "DELETE FROM participation WHERE id_participation = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idParticipation);
            ps.executeUpdate();
            System.out.println("✅ Participation avec id = " + idParticipation + " supprimée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= GET PARTICIPATIONS BY USER =================
    @Override
    public List<Participation> getParticipationsByUser(int userId) {
        List<Participation> participations = new ArrayList<>();
        String req = "SELECT * FROM participation WHERE id_user = ? ORDER BY date_participation DESC";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Participation p = new Participation(
                        rs.getInt("id_participation"),
                        rs.getInt("id_user"),
                        rs.getInt("id_event"),
                        rs.getTimestamp("date_participation").toLocalDateTime(),
                        rs.getInt("nbrP")
                );
                participations.add(p);
            }
            System.out.println("📊 " + participations.size() + " participation(s) trouvée(s) pour l'utilisateur " + userId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return participations;
    }

    // ================= GET PARTICIPATIONS BY EVENT =================
    @Override
    public List<Participation> getParticipationsByEvent(int eventId) {
        List<Participation> participations = new ArrayList<>();
        String req = "SELECT * FROM participation WHERE id_event = ? ORDER BY date_participation DESC";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Participation p = new Participation(
                        rs.getInt("id_participation"),
                        rs.getInt("id_user"),
                        rs.getInt("id_event"),
                        rs.getTimestamp("date_participation").toLocalDateTime(),
                        rs.getInt("nbrP")
                );
                participations.add(p);
            }
            System.out.println("📊 " + participations.size() + " participant(s) trouvé(s) pour l'événement " + eventId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return participations;
    }

    // ================= VÉRIFIER SI DÉJÀ INSCRIT =================
    @Override
    public boolean estDejaInscrit(int userId, int eventId) {
        String req = "SELECT COUNT(*) FROM participation WHERE id_user = ? AND id_event = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean inscrit = rs.getInt(1) > 0;
                System.out.println("🔍 Utilisateur " + userId + " déjà inscrit à l'événement " + eventId + " ? " + inscrit);
                return inscrit;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // ================= COMPTER PARTICIPANTS PAR ÉVÉNEMENT =================
    @Override
    public int countParticipantsByEvent(int eventId) {
        String req = "SELECT COUNT(*) FROM participation WHERE id_event = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("📊 " + count + " inscription(s) pour l'événement " + eventId);
                return count;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    // ========== NOUVELLES MÉTHODES POUR nbrP ==========

    /**
     * Calcule le nombre TOTAL de participants (somme des nbrP) pour un événement
     */
    @Override
    public int getTotalParticipantsReservedForEvent(int eventId) {
        String req = "SELECT COALESCE(SUM(nbrP), 0) FROM participation WHERE id_event = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int total = rs.getInt(1);
                System.out.println("📊 Total des places réservées pour l'événement " + eventId + " : " + total);
                return total;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * Vérifie si un utilisateur peut réserver un certain nombre de places
     */
    @Override
    public boolean checkAvailableSpots(int eventId, int requestedSpots) {
        // Note: Cette méthode nécessite un EventService pour récupérer l'événement
        // Tu devras injecter EventService ou modifier la logique
        System.out.println("⚠️ checkAvailableSpots nécessite EventService pour fonctionner complètement");
        return true; // Temporaire - à implémenter avec EventService
    }

    /**
     * Récupère le nombre de places réservées par un utilisateur pour un événement spécifique
     */
    @Override
    public int getUserReservedSpotsForEvent(int userId, int eventId) {
        String req = "SELECT COALESCE(SUM(nbrP), 0) FROM participation WHERE id_user = ? AND id_event = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int spots = rs.getInt(1);
                System.out.println("📊 Utilisateur " + userId + " a réservé " + spots + " place(s) pour l'événement " + eventId);
                return spots;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * Met à jour le nombre de places pour une participation existante
     */
    @Override
    public void updateParticipationSpots(int idParticipation, int newNbrP) {
        String req = "UPDATE participation SET nbrP = ? WHERE id_participation = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, newNbrP);
            ps.setInt(2, idParticipation);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Participation " + idParticipation + " mise à jour avec " + newNbrP + " place(s)");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Récupère une participation par son ID
     */

    public Participation getParticipationById(int idParticipation) {
        String req = "SELECT * FROM participation WHERE id_participation = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idParticipation);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Participation(
                        rs.getInt("id_participation"),
                        rs.getInt("id_user"),
                        rs.getInt("id_event"),
                        rs.getTimestamp("date_participation").toLocalDateTime(),
                        rs.getInt("nbrP")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // ================= MÉTHODES SUPPLÉMENTAIRES UTILES =================

    /**
     * Supprimer toutes les participations d'un utilisateur
     */
    public void supprimerParticipationsByUser(int userId) {
        String req = "DELETE FROM participation WHERE id_user = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            System.out.println("✅ " + rows + " participation(s) supprimée(s) pour l'utilisateur " + userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Supprimer toutes les participations d'un événement
     */
    public void supprimerParticipationsByEvent(int eventId) {
        String req = "DELETE FROM participation WHERE id_event = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, eventId);
            int rows = ps.executeUpdate();
            System.out.println("✅ " + rows + " participation(s) supprimée(s) pour l'événement " + eventId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Version complète de checkAvailableSpots (si tu as accès à EventService)
     * Décommente et utilise cette version si tu peux injecter EventService
     */
    /*
    private EventService eventService = new EventService(); // À injecter proprement

    @Override
    public boolean checkAvailableSpots(int eventId, int requestedSpots) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            System.out.println("❌ Événement " + eventId + " non trouvé");
            return false;
        }

        int totalReserved = getTotalParticipantsReservedForEvent(eventId);
        boolean available = (totalReserved + requestedSpots) <= event.getMaxParticipant();

        System.out.println("🔍 Places disponibles pour événement " + eventId +
                          " : " + (event.getMaxParticipant() - totalReserved) +
                          " / " + event.getMaxParticipant() +
                          " (demandé: " + requestedSpots + ")");

        return available;
    }
    */
}