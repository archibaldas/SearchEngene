package searchengine.model.services;

import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;
import searchengine.model.entity.SiteEntity;

import java.util.List;
import java.util.Map;

public interface IndexService {
    List<SearchIndex> findAllByPage(Page page);
    SearchIndex findById(Long id);
    List<SearchIndex> findByLemma(Lemma lemma);
    int count();
    SearchIndex update(SearchIndex entity);
    void deleteAllByList(List<SearchIndex> indexes);
    void createIndexList(Page page, List<Lemma> lemmas, Map<String, Integer> lemmaNap);
    List<Page> findPagesByAllLemmas(List<String> lemmas, SiteEntity site, long lemmaCount);
    List<Page> findPagesByAllLemmasGlobal(List<String> lemmas, long lemmaCount);
    Map<Page, Float> findPagesWithAbsRel(List<Page> pages, List<String> lemmas);
    Map<Page,Float> findPagesByAllLemmasGlobalWithRank(List<String> lemmas, long lemmaCount);
}
