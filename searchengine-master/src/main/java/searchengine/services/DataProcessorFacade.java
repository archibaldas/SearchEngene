package searchengine.services;

import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.core.dto.PageDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

public interface DataProcessorFacade {

    SiteEntity findSite(Site site);
    void processedStatus(SiteEntity siteEntity);
    SiteEntity saveStatus(SiteEntity siteEntity);
    SiteEntity saveStatus(SiteEntity siteEntity, String error);
    Page savePageOrIgnore(PageDto pageDto);
    SiteEntity createOrRecreateIfExistSite(Site site);
    boolean existsPageLinkInDatabase(String url);
    void deleteNoListSitesFromDb(SitesList sitesList);
    void deletePageWithDataByUrl(String url);
}
