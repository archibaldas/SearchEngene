package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    List <Page> findByPathAndSite(String path, SiteEntity site);
    boolean existsByPathAndSite(String path, SiteEntity site);

    List<Page> findAllBySite(SiteEntity site);
}