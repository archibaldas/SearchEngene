package searchengine.model.services;

import searchengine.core.dto.PageDto;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

import java.util.List;

public interface PageService {
    List<Page> findByPathAndSite(String path, SiteEntity siteEntity);
    List<Page> findAllBySite(SiteEntity siteEntity);
    long countBySite(SiteEntity siteEntity);
    Page savePageOrIgnore(PageDto pageDto);
    boolean existsPageLinkInDatabase(String url);
    int count();
    void delete(Page entity);
}

