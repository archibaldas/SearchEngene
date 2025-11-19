package searchengine.model.services;

import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;

import java.time.Instant;
import java.util.List;

public interface SiteService extends CRUDService<SiteEntity> {
    List<SiteEntity> findByStatus(StatusType statusType);
    SiteEntity findById(Long id);
    List<SiteEntity> findAll();
    SiteEntity findByUrl(String url);
    boolean existsByUrl(String url);
    void updateStatus(Long id, StatusType status, Instant statusTime);
    void updateErrorStatus(Long id, StatusType status, Instant statusTime, String error);

}
