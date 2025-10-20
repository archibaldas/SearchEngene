package searchengine.mapper;

import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.model.entity.SiteEntity;
import searchengine.services.dto.statistics.DetailedStatisticsItem;

@Component
public interface SiteMapper {
    SiteEntity dtoToEntity(Site site);
    DetailedStatisticsItem entityToDetailedStatisticResult(SiteEntity siteEntity);
    SiteEntity setIndexing(SiteEntity siteEntity);
    SiteEntity setIndexed(SiteEntity siteEntity);
    SiteEntity setFailed(SiteEntity siteEntity, String errorText);
}
