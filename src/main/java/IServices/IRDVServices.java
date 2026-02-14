package IServices;

import Entites.RDV;
import java.sql.SQLException;
import java.util.List;

public interface IRDVServices {
    void ajouterRDV(RDV rdv) throws SQLException;
    void supprimerRDV(int id);
    void modifierRDV(RDV rdv);
    List<RDV> afficherRDV();
    RDV getRDVById(int id);
    List<RDV> afficherRDVByStatut(String statut);
}
