package searchengine.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import searchengine.config.Site;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;
import searchengine.web.services.dto.statistics.DetailedStatisticsItem;

import java.time.Instant;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
imports = {Instant.class, StatusType.class})
public interface SiteMapper {

    @Mapping(target = "url", expression = "java( searchengine.core.utils.HtmlUtils.normalizeUrl(site.getUrl()) )")
    @Mapping(target = "statusTime", expression = "java( java.time.Instant.now() )")
    @Mapping(target = "status", expression = "java( searchengine.model.entity.StatusType.INDEXING )")
    @Mapping(target = "lastError", constant = "")
    SiteEntity dtoToEntity(Site site);

    @Mapping(target = "status", expression = "java( siteEntity.getStatus().name() )")
    @Mapping(target = "statusTime", expression = "java( siteEntity.getStatusTime().toEpochMilli() )")
    @Mapping(source = "lastError", target = "error")
    DetailedStatisticsItem entityToDetailedStatisticResult(SiteEntity siteEntity);

    @Mapping(target = "url", expression = "java( searchengine.core.utils.HtmlUtils.normalizeUrl(site.getUrl()) )")
    @Mapping(target = "status", constant = "")
    @Mapping(target = "error", constant = "")
    @Mapping(target = "statusTime", constant = "0L")
    DetailedStatisticsItem noIndexedDataToDetailed(Site site);
}
