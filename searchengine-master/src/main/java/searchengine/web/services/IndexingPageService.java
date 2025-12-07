package searchengine.web.services;

import searchengine.core.dto.ExtractedDataFromPage;

import java.util.List;

public interface IndexingPageService {
    List<String> indexing (String url);
    void saveLemmasAndIndexes(ExtractedDataFromPage data);
    void deletePageWithDataByUrl(String url);
}
