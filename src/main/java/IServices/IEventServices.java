package IServices;



import Entites.Event;
import java.util.List;

public interface IEventServices {

    boolean ajouter(Event event);

    boolean modifier(Event event);

    boolean supprimer(int id);

    List<Event> getAll();
}
