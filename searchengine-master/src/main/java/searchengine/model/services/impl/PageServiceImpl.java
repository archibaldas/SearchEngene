package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.core.utils.BeanUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;
import searchengine.model.repositories.PageRepository;
import searchengine.model.services.PageService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    @Override
    public Page findById(Long id) throws NoFoundEntityException{
        return pageRepository.findById(id).orElseThrow(() ->
                new NoFoundEntityException("поиска страницы по ID", id, "Поиск сраницы по ID не дал результата"));
    }

    @Override
    public List<Page> findByPathAndSite(String path, SiteEntity siteEntity) throws NoFoundEntityException {
        return pageRepository.findByPathAndSite(path,siteEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Page> findAllBySite(SiteEntity siteEntity) {
        return pageRepository.findAllBySite(siteEntity);
    }

    @Override
    public int count() {
        return Math.toIntExact(pageRepository.count());
    }

    @Override
    public long countBySite(SiteEntity siteEntity) {
        return findAllBySite(siteEntity).size();
    }

    @Override
    public boolean existsByPathAndSite(String path, SiteEntity site) {
        return pageRepository.existsByPathAndSite(path, site);
    }

    @Override
    @Transactional
    public Page create(Page entity) {
        return pageRepository.save(entity);
    }

    @Override
    @Transactional
    public Page update(Page entity) {
        Page existingPage = findById(entity.getId());
        BeanUtils.copyNotNullProperties(entity, existingPage);
        return pageRepository.save(existingPage);
    }

    @Override
    @Transactional
    public void delete(Page entity) {
        pageRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteAllByList(List<Page> pages) {
        pageRepository.deleteAll(pages);

    }
}
