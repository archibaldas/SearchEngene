package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;
import searchengine.model.entity.SiteEntity;
import searchengine.model.projection.GlobalSearchProjection;
import searchengine.model.projection.PageRankProjection;

import java.util.List;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {

    List<SearchIndex> findByLemma(Lemma lemma);
    List<SearchIndex> findAllByPage(Page page);

    @Query("SELECT si.page FROM SearchIndex si " +
    "WHERE si.lemma.lemma IN :lemmas AND si.lemma.site = :site " +
    "GROUP BY si.page " +
    "HAVING COUNT(DISTINCT si.lemma) = :lemmaCount")
    List<Page> findPagesByAllLemmas(@Param("lemmas") List<String> lemmas,
                                    @Param("site")SiteEntity site,
                                    @Param("lemmaCount") long lemmaCount);

    @Query("SELECT si.page AS page, SUM(si.rank) as totalRank FROM SearchIndex si " +
    "WHERE si.lemma.lemma IN :lemmas " +
    "GROUP BY si.page " +
    "HAVING COUNT(DISTINCT si.lemma) = :lemmaCount")
    List<GlobalSearchProjection> findPagesByAllLemmasGlobal(@Param("lemmas") List<String> lemmas,
                                                            @Param("lemmaCount") long lemmaCount);

    @Query("SELECT si.page AS page, SUM(si.rank) as totalRank FROM SearchIndex si " +
            "WHERE si.page IN :pages AND si.lemma.lemma IN :lemmas " +
            "GROUP BY si.page")
    List<PageRankProjection> findPagesRanks(@Param("pages") List<Page> pages,
                                            @Param("lemmas") List<String> lemmas);

}