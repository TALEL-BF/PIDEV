package IServices;

import Entites.Conseil;
import java.util.List;

public interface IConseilServices {
    void ajouterConseil(Conseil c);
    void modifierConseil(Conseil c);
    void supprimerConseil(int idArticle);
    List<Conseil> afficherConseils();
    Conseil getConseilById(int idArticle);

    // optionnel: recherche DB
    List<Conseil> rechercher(String keyword);
    int incrementLike(int idArticle);
    int decrementLike(int idArticle);
}
