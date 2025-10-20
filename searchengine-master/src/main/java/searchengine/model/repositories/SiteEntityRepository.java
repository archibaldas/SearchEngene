package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteEntityRepository extends JpaRepository<SiteEntity, Long> {
    Optional<SiteEntity> findByUrl(String url);
    boolean existsByUrl(String url);
    List<SiteEntity> findByStatus(StatusType statusType);
}