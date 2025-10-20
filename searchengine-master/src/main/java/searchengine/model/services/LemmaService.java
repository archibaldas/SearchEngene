package searchengine.model.services;

import searchengine.model.entity.Lemma;
import searchengine.model.entity.SiteEntity;

import java.util.List;

public interface LemmaService extends CRUDService<Lemma>{
    List<Lemma> findAllBySite(SiteEntity siteEntity);
    Lemma findById(Long id);
    Lemma findByLemmaAndSite(String lemma, SiteEntity site);
    List<Lemma> findByLemma(String lemma);
}
