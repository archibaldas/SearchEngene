package searchengine.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import searchengine.core.dto.PageDto;
import searchengine.mapper.delegates.PageMapperDelegate;
import searchengine.model.entity.Page;

@DecoratedWith(PageMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PageMapper {
    Page dtoToEntity(PageDto dto);
}
