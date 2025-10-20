package searchengine.mapper;

import searchengine.core.dto.PageDto;
import searchengine.model.entity.Page;

public interface PageMapper {
    Page dtoToEntity(PageDto dto);
}
