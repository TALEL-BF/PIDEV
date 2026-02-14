package IServices;

import Entites.Seance;
import java.sql.SQLException;
import java.util.List;

public interface ISeanceServices {
    void ajouterSeance(Seance seance) throws SQLException;
    void supprimerSeance(int id);
    void modifierSeance(Seance seance);
    List<Seance> afficherSeances();
    Seance getSeanceById(int id);
    List<Seance> afficherSeancesByStatut(String statut);
}

