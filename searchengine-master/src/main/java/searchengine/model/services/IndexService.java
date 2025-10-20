package searchengine.model.services;

import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;

import java.util.List;

public interface IndexService extends CRUDService<SearchIndex> {
    List<SearchIndex> findAllByPage(Page page);
    SearchIndex findById(Long id);
    SearchIndex findByLemmaAndPage(Lemma lemma, Page page);
    List<SearchIndex> findByLemma(Lemma lemma);
}
