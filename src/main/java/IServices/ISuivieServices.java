package IServices;

import Entites.Suivie;
import java.sql.SQLException;
import java.util.List;

public interface ISuivieServices {

        void ajouterSuivie(Suivie s) throws SQLException;
        void supprimerSuivie(int idSuivie) throws SQLException;
        void modifierSuivie(Suivie s) throws SQLException;
        List<Suivie> afficherSuivie() throws SQLException;

}
