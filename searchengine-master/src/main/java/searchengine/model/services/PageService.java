package searchengine.model.services;

import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

import java.util.List;

public interface PageService extends CRUDService<Page> {
    Page findById(Long id);
    Page findByPathAndSite(String path, SiteEntity siteEntity);
    List<Page> findAllBySite(SiteEntity siteEntity);
    long countBySite(SiteEntity siteEntity);
    boolean existsByPathAndSite(String path, SiteEntity site);
}
