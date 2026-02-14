package IServices;

import Entites.EmploiDuTemps;
import java.sql.SQLException;
import java.util.List;

public interface IEmploiDuTempsServices {
    void ajouterEmploi(EmploiDuTemps emploi) throws SQLException;
    void supprimerEmploi(int id);
    void modifierEmploi(EmploiDuTemps emploi);
    List<EmploiDuTemps> afficherEmplois();
    EmploiDuTemps getEmploiById(int id);
    List<EmploiDuTemps> afficherEmploisByJour(String jour);
}

