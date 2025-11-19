package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteEntityRepository extends JpaRepository<SiteEntity, Long> {
    Optional<SiteEntity> findByUrl(String url);
    boolean existsByUrl(String url);
    List<SiteEntity> findByStatus(StatusType statusType);

    @Modifying(clearAutomatically = true)
    @Query("update SiteEntity s set s.status = :status, s.statusTime= :statusTime where s.id = :id")
    void updateStatus(@Param("id") Long id,
                      @Param("status") StatusType status,
                      @Param("statusTime") Instant statusTime);

    @Modifying(clearAutomatically = true)
    @Query("update SiteEntity s set s.status = :status, s.statusTime = :statusTime, s.lastError = :lastError where s.id = :id ")
    void updateErrorStatus(@Param("id") Long id,
                           @Param("status") StatusType status,
                           @Param("statusTime") Instant statusTime,
                           @Param("lastError") String lastError);
}