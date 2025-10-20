package searchengine.mapper.delegates;

import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.core.utils.HtmlUtils;
import searchengine.mapper.SiteMapper;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;
import searchengine.services.dto.statistics.DetailedStatisticsItem;

import java.time.Instant;

@Component
public class SiteMapperDelegate implements SiteMapper {
    @Override
    public SiteEntity dtoToEntity(Site site) {
        SiteEntity entity = new SiteEntity();
        entity.setName(site.getName());
        entity.setUrl(HtmlUtils.normalizeUrl(site.getUrl()));
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
        item.setPages(siteEntity.getPages().size());
        item.setLemmas(siteEntity.getLemmas().size());
        return item;
    }

    @Override
    public SiteEntity setIndexing(SiteEntity siteEntity) {
        siteEntity.setStatusTime(Instant.now());
        siteEntity.setStatus(StatusType.INDEXING);
        return siteEntity;
    }

    @Override
    public SiteEntity setIndexed(SiteEntity siteEntity) {
        siteEntity.setStatusTime(Instant.now());
        siteEntity.setStatus(StatusType.INDEXED);
        return siteEntity;
    }

    @Override
    public SiteEntity setFailed(SiteEntity siteEntity, String errorText) {
        siteEntity.setStatusTime(Instant.now());
        siteEntity.setStatus(StatusType.FAILED);
        siteEntity.setLastError(errorText);
        return siteEntity;
    }
}
