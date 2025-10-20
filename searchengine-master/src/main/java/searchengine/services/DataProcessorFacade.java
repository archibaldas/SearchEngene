package searchengine.services;

import searchengine.config.Site;
import searchengine.core.dto.PageDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

public interface DataProcessorFacade {
    SiteEntity findOrCreateSiteInDatabase(Site site);
    void processedStatus(SiteEntity siteEntity);
    SiteEntity saveStatus(SiteEntity siteEntity);
    SiteEntity saveStatus(SiteEntity siteEntity, String error);
    Page saveOrUpdatePageToDatabase(PageDto pageDto);
    void deleteAllBySite(Site site);
    void saveIndexFromDto(SearchIndexDto searchIndexDto);
    boolean existsPageLinkInDatabase(String url);
}
