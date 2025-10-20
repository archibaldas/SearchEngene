package searchengine.mapper;

import searchengine.core.dto.SearchIndexDto;
import searchengine.model.entity.SearchIndex;

public interface SearchIndexMapper {
    SearchIndex dtoToEntity(SearchIndexDto indexDto);
}
