package searchengine.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import searchengine.config.Site;
import searchengine.mapper.delegates.SiteMapperDelegate;
import searchengine.model.entity.SiteEntity;
import searchengine.web.services.dto.statistics.DetailedStatisticsItem;

@DecoratedWith(SiteMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SiteMapper {
    SiteEntity dtoToEntity(Site site);
    DetailedStatisticsItem entityToDetailedStatisticResult(SiteEntity siteEntity);
    DetailedStatisticsItem noIndexedDataToDetailed(Site site);
}
