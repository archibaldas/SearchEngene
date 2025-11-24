package searchengine.services;

import searchengine.core.dto.ExtractedDataFromPage;
import searchengine.core.dto.SearchIndexDto;

import java.util.List;

public interface IndexingPageService {
    List<String> indexing (String url);
    void saveLemmasAndIndexes(List<SearchIndexDto> unusedDtos, ExtractedDataFromPage data);
}
