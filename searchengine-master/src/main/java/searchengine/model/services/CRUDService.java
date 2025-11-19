package searchengine.model.services;

import java.util.List;

public interface CRUDService <T>{
    int count();
    T create (T entity);
    T update (T entity);
    void delete(T entity);
    void deleteAllByList(List<T> entity);
}
