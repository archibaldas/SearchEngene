package searchengine.model.services;

import searchengine.model.entity.Lemma;
import searchengine.model.entity.SiteEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LemmaService {
    Lemma findById(Long id);
    void updateLemmasForSite(SiteEntity site, Map<String,Integer> lemmasOnPage);
    int count();
    void update(Lemma entity);
    void delete(Lemma entity);
    List<Lemma> findBySiteAndLemmaIn(SiteEntity site, Set<String> lemmaSet);
    Map<String, Integer> getFrequenciesBySiteAndLemmas(SiteEntity site, List<String> lemmas);
    Map<String, Integer> getGlobalFrequenciesByLemmas(List<String> lemmas);
}
