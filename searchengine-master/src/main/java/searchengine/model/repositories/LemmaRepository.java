package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.SiteEntity;
import searchengine.model.projection.GlobalLemmaFrequencyProjection;
import searchengine.model.projection.LemmaFrequencyProjection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    Optional<Lemma> findByLemmaAndSite(String lemma, SiteEntity site);
    List<Lemma> findByLemma(String lemma);
    List<Lemma> findAllBySite(SiteEntity site);
    List<Lemma> findBySiteAndLemmaIn(SiteEntity site, Collection<String> lemma);

    int countBySite(SiteEntity site);

    @Query("SELECT l.lemma, l.frequency FROM Lemma l WHERE l.site = :site AND l.lemma IN :lemmas")
    List<LemmaFrequencyProjection> findFrequenciesBySiteAndLemmas(@Param("site") SiteEntity site,
                                                                  @Param("lemmas") List<String> lemmas);
    @Query("SELECT l.lemma AS lemma, COALESCE(SUM(l.frequency), 0) AS totalFrequency FROM Lemma l WHERE l.lemma IN :lemmas GROUP BY l.lemma")
    List<GlobalLemmaFrequencyProjection> findGlobalFrequenciesByLemmas(@Param("lemmas") List<String> lemmas);
}