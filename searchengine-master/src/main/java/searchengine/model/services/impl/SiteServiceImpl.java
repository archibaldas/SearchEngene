package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.core.utils.BeanUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;
import searchengine.model.repositories.SiteEntityRepository;
import searchengine.model.services.SiteService;

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
        return siteEntityRepository.findById(id).orElseThrow(() -> new NoFoundEntityException("поиска сайта", id, "Сайт с таким ID не сохраненен в базе данных"));
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
    public SiteEntity create(SiteEntity entity) {
        return siteEntityRepository.save(entity);
    }

    @Override
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
    public void delete(SiteEntity entity) {
        siteEntityRepository.delete(entity);
    }

    @Override
    public boolean existsByUrl(String url) {
        return siteEntityRepository.existsByUrl(url);
    }
}
