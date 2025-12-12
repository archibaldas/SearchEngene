package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.core.dto.PageDto;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.exceptions.PageIndexingException;
import searchengine.mapper.PageMapper;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;
import searchengine.model.repositories.PageRepository;
import searchengine.model.services.PageService;

import java.net.MalformedURLException;
import java.util.List;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    @Override
    public Page savePageOrIgnore(PageDto pageDto) {
        Page page = pageMapper.dtoToEntity(pageDto);
        try{
            return pageRepository.save(page);
        } catch (DataIntegrityViolationException e){
            throw new PageIndexingException("[", pageDto.getSite().getUrl(),
                    pageDto.getPath(), "] Страница уже проиндексирована");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsPageLinkInDatabase(String url) {
        String[] splitLink = null;
        try {
            splitLink = new String[]{
                    HtmlUtils.getBaseUrl(url),
                    HtmlUtils.getPath(url)
            };
        } catch (MalformedURLException e) {
            log.warn("Ссылка: {} указана ошибочно. Текст ошибки {}",  url, e.getMessage());
        }
        return pageRepository.existsBySiteUrlAndPath(splitLink[0], splitLink[1]) ||
                pageRepository.existsBySiteUrlAndPath(splitLink[0], normalizeUrl(splitLink[1]));
    }

    @Override
    public List<Page> findByPathAndSite(String path, SiteEntity siteEntity) throws NoFoundEntityException {
        List<Page> pages = pageRepository.findByPathAndSite(path,siteEntity);
        if (pages.isEmpty()) throw new NoFoundEntityException("Страница c адресом:", siteEntity.getUrl() + path, "не сохранена в базе данных." );
        return pages;
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
    @Transactional
    public void delete(Page entity) {
        pageRepository.delete(entity);
    }
}
