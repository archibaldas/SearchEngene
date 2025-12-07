package searchengine.model.services;

import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;
import searchengine.web.services.dto.statistics.DetailedStatisticsItem;

import java.util.List;

public interface SiteService {
    List<SiteEntity> findByStatus(StatusType statusType);
    SiteEntity findById(Long id);
    List<SiteEntity> findAll();
    SiteEntity findByUrl(String url);
    boolean existsByUrl(String url);
    SiteEntity findSite(Site site);
    SiteEntity create(Site site);
    SiteEntity updateStatus(SiteEntity siteEntity);
    int count();
    SiteEntity create(SiteEntity entity);
    SiteEntity getSiteEntityByLink(String link);
    DetailedStatisticsItem getDetailedStatisticItemBySite(Site site);
    List<DetailedStatisticsItem> getDetailedStatisticItems(SitesList sites);
}
