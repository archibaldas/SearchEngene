package searchengine.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import searchengine.core.dto.PageDto;
import searchengine.model.entity.Page;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "indexes", ignore = true)
    Page dtoToEntity(PageDto dto);
}
