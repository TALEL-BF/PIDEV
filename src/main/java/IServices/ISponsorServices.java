package IServices;


import Entites.Event;
import Entites.Sponsor;

import java.util.List;



import java.util.List;

public interface ISponsorServices<T> {
    boolean ajouter(T t);
    boolean modifier(T t);
    boolean supprimer(int id);
    List<T> getAll();
}

