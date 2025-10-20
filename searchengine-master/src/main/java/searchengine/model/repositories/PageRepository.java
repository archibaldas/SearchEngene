package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findByPathAndSite(String path, SiteEntity site);
    boolean existsByPathAndSite(String path, SiteEntity site);

    List<Page> findAllBySite(SiteEntity site);
}