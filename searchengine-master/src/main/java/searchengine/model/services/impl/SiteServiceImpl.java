package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.core.utils.BeanUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;
import searchengine.model.repositories.SiteEntityRepository;
import searchengine.model.services.SiteService;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteEntityRepository siteEntityRepository;

    @Override
    public List<SiteEntity> findByStatus(StatusType statusType) {
        return siteEntityRepository.findByStatus(statusType);
    }

    @Override
    public SiteEntity findById(Long id) {
        return siteEntityRepository.findById(id).orElseThrow(
                () -> new NoFoundEntityException("поиска сайта", id, "Сайт с таким ID не сохраненен в базе данных"));
    }

    @Override
    public List<SiteEntity> findAll() {
        return siteEntityRepository.findAll();
    }

    @Override
    public SiteEntity findByUrl(String url) {
        return siteEntityRepository.findByUrl(url).orElseThrow(
                () -> new NoFoundEntityException("поиска сайта", url, "Сайт с таким адресом не сохранен в базе данных"));
    }

    @Override
    public int count() {
        return Math.toIntExact(siteEntityRepository.count());
    }

    @Override
    @Transactional
    public SiteEntity create(SiteEntity entity) {
        return siteEntityRepository.save(entity);
    }

    @Override
    @Transactional
    public SiteEntity update(SiteEntity entity) throws NoFoundEntityException {
        if(entity == null){
            log.warn("Entity is null");
            throw new NoFoundEntityException("entity is null", "null", "null");
        } else {
            SiteEntity existingSite = findById(entity.getId());
            BeanUtils.copyNotNullProperties(entity, existingSite);
            return siteEntityRepository.save(existingSite);
        }
    }

    @Override
    @Transactional
    public void delete(SiteEntity entity) {
        siteEntityRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteAllByList(List<SiteEntity> sites) {
        siteEntityRepository.deleteAll(sites);

    }

    @Override
    public boolean existsByUrl(String url) {
        return siteEntityRepository.existsByUrl(url);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, StatusType status, Instant statusTime) {
        siteEntityRepository.updateStatus(id, status, statusTime);
    }

    @Override
    @Transactional
    public void updateErrorStatus(Long id, StatusType status, Instant statusTime, String error) {
        siteEntityRepository.updateErrorStatus(id, status, statusTime, error);
    }
}
