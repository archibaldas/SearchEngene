package searchengine.services;

public interface SearchService {
    Object search(String query, String siteUrl, int offset, int limit);
}
