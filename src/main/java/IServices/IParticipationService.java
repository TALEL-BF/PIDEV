package com.auticare.iservices;

import com.auticare.entities.Participation;
import java.sql.SQLException;
import java.util.List;

public interface IParticipationService {

    // ===== MÉTHODES CRUD DE BASE =====
    void ajouterParticipation(Participation participation) throws SQLException;
    void supprimerParticipation(int idParticipation);
    void modifierParticipation(Participation participation);
    List<Participation> afficherParticipations();

    // ===== MÉTHODES DE RECHERCHE PAR RELATION =====
    List<Participation> getParticipationsByUser(int userId);
    List<Participation> getParticipationsByEvent(int eventId);
    Participation getParticipationById(int idParticipation);

    // ===== MÉTHODES DE VÉRIFICATION =====
    boolean estDejaInscrit(int userId, int eventId);
    int countParticipationsByEvent(int eventId);

    // ===== NOUVELLES MÉTHODES POUR nbrP =====
    int getTotalParticipantsReservedForEvent(int eventId);
    boolean checkAvailableSpots(int eventId, int requestedSpots) throws SQLException;
    int getUserReservedSpotsForEvent(int userId, int eventId);
    void updateParticipationSpots(int idParticipation, int newNbrP);

    // ===== MÉTHODES DE SUPPRESSION PAR LOT =====
    void supprimerParticipationsByUser(int userId);
    void supprimerParticipationsByEvent(int eventId);
}