package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {
    Optional<SearchIndex> findByLemmaAndPage(Lemma lemma, Page page);

    List<SearchIndex> findByLemma(Lemma lemma);

    List<SearchIndex> findAllByPage(Page page);
}