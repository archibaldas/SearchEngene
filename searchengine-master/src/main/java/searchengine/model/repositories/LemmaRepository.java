package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    Optional<Lemma> findByLemmaAndSite(String lemma, SiteEntity site);

    List<Lemma> findByLemma(String lemma);

    List<Lemma> findAllBySite(SiteEntity site);
}