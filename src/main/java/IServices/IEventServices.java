package com.auticare.iservices;

import com.auticare.entities.Event;
import com.auticare.entities.Sponsor;

import java.sql.SQLException;
import java.util.List;

public interface IEventServices {

    void ajoutEvent(Event event) throws SQLException;
    void supprimerEvent(int idEvent);
    void modifierEvent(Event event);
    List<Event> afficherEvent();

    // ===== MÉTHODES POUR SPONSORS =====
    List<Sponsor> getSponsorsForEvent(int idEvent);
    void addSponsorToEvent(int idEvent, int idSponsor);
    void removeAllSponsorsFromEvent(int idEvent);
    void supprimerPlanning(int idEvent);
    Event getEventById(int idEvent);
}