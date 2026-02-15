package IServices;

import Entites.StatsMensuelle;
import Entites.StatsSeanceDelta;

import java.util.List;

public interface IStatsServices {

    // Liste des enfants (pour ComboBox)
    List<String> getAllEnfants();

    // Stat 1 : comparaison dernière séance vs séance précédente
    StatsSeanceDelta getLastDelta(String nomEnfant);

    // Stat 2 : avancement global mensuel (moyennes par mois)
    List<StatsMensuelle> getMonthlyStats(String nomEnfant);
}
