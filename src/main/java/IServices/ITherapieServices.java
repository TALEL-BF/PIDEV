package IServices;

import Entites.Therapie;
import java.sql.SQLException;
import java.util.List;

public interface ITherapieServices {

        void ajouterTherapie(Therapie t) throws SQLException;
        void supprimerTherapie(int idTherapie) throws SQLException;
        void modifierTherapie(Therapie t) throws SQLException;
        List<Therapie> afficherTherapie() throws SQLException;

}
