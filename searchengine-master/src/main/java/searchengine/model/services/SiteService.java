package searchengine.model.services;

import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.entity.SiteEntity;
import searchengine.web.services.dto.statistics.DetailedStatisticsItem;

import java.util.List;

public interface SiteService {
    SiteEntity findByUrl(String url);
    boolean existsByUrl(String url);
    SiteEntity findSite(Site site);
    SiteEntity create(Site site);
    void updateStatus(SiteEntity siteEntity);
    int count();
    SiteEntity create(SiteEntity entity);
    SiteEntity getSiteEntityByLink(String link);
    DetailedStatisticsItem getDetailedStatisticItemBySite(Site site);
    List<DetailedStatisticsItem> getDetailedStatisticItems(SitesList sites);
}
