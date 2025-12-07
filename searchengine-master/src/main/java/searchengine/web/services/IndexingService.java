package searchengine.web.services;

public interface IndexingService {
    void startIndexing();
    void stopIndexing();
    void indexPage(String url);
}
