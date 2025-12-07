package searchengine.mapper.delegates;

import searchengine.config.Site;
import searchengine.mapper.SiteMapper;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;
import searchengine.web.services.dto.statistics.DetailedStatisticsItem;

import java.time.Instant;

import static searchengine.core.utils.HtmlUtils.normalizeUrl;


public abstract class SiteMapperDelegate implements SiteMapper {
    @Override
    public SiteEntity dtoToEntity(Site site) {
        SiteEntity entity = new SiteEntity();
        entity.setName(site.getName());
        entity.setUrl(normalizeUrl(site.getUrl()));
        entity.setStatusTime(Instant.now());
        entity.setStatus(StatusType.INDEXING);
        entity.setLastError("");
        return entity;
    }

    @Override
    public DetailedStatisticsItem entityToDetailedStatisticResult(SiteEntity siteEntity) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(siteEntity.getName());
        item.setUrl(siteEntity.getUrl());
        item.setStatus(siteEntity.getStatus().name());
        item.setStatusTime(siteEntity.getStatusTime().toEpochMilli());
        return item;
    }

    @Override
    public DetailedStatisticsItem noIndexedDataToDetailed(Site site){
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setUrl(normalizeUrl(site.getUrl()));
        item.setName(site.getName());
        return item;
    }
}
