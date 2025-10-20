package searchengine.model.services;

import org.springframework.transaction.annotation.Transactional;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;

import java.util.List;

public interface SiteService extends CRUDService<SiteEntity> {
    List<SiteEntity> findByStatus(StatusType statusType);
    SiteEntity findById(Long id);
    List<SiteEntity> findAll();
    @Transactional
    SiteEntity findByUrl(String url);
    boolean existsByUrl(String url);

}
