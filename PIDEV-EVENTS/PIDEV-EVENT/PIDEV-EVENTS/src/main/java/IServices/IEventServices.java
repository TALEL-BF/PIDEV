package IServices;

import Entites.Event;
import Entites.Sponsor;

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
}