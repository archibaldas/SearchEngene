package searchengine.model.services;

import org.springframework.transaction.annotation.Transactional;

public interface CRUDService <T>{
    int count();
    @Transactional
    T create (T entity);
    @Transactional
    T update (T entity);
    @Transactional
    void delete(T entity);
}
