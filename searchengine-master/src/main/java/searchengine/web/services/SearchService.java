package searchengine.web.services;

import searchengine.web.services.dto.responses.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, String siteUrl, int offset, int limit);
}
