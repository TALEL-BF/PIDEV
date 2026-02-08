package IServices;

import java.util.List;

public interface IServices<T> {
    boolean ajouter(T t);
    boolean modifier(T t);
    boolean supprimer(int id);
    List<T> getAll();
}

