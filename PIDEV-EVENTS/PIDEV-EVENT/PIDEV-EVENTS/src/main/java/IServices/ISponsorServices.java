package IServices;

import Entites.Event;
import Entites.Sponsor;

import java.sql.SQLException;
import java.util.List;

public interface ISponsorServices {

    void ajoutSponsor(Sponsor sponsor) throws SQLException;
    void supprimerSponsor(int idSponsor);
    void modifierSponsor(Sponsor sponsor);
    List<Sponsor> afficherSponsor();
    Sponsor getSponsorById(int idSponsor);
    List<Sponsor> rechercherSponsor(String keyword);
    List<Sponsor> getTopSponsors();
    List<Event> getEventsForSponsor(int sponsorId);
}