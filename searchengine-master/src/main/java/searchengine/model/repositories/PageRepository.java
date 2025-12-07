package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    List <Page> findByPathAndSite(String path, SiteEntity site);

    @Query("SELECT COUNT(p) > 0 FROM Page p WHERE p.site.url = :siteUrl AND p.path = :path")
    boolean existsBySiteUrlAndPath(@Param("siteUrl") String siteUrl, @Param("path") String path);

    List<Page> findAllBySite(SiteEntity site);

    int countBySite(SiteEntity site);

    @Query("SELECT p FROM Page p WHERE p.id IN :pageIds")
    List<Page> findByIds(@Param("pageIds") List<Long> pageIds);
}