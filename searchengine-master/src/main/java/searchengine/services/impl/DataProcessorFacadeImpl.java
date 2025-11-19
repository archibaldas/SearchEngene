package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.core.dto.PageDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.exceptions.PageIndexingException;
import searchengine.mapper.PageMapper;
import searchengine.mapper.SearchIndexMapper;
import searchengine.mapper.SiteMapper;
import searchengine.model.entity.*;
import searchengine.model.services.*;
import searchengine.services.DataProcessorFacade;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.List;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessorFacadeImpl implements DataProcessorFacade {

    private final SiteService siteService;
    private final SiteMapper siteMapper;
    private final PageService pageService;
    private final PageMapper pageMapper;
    private final LemmaService lemmaService;
    private final SearchIndexMapper searchIndexMapper;
    private final IndexService indexService;

    @Override
    public SiteEntity findSite(Site site) {
            return siteService.findByUrl(normalizeUrl(site.getUrl()));
    }

    @Override
    public void processedStatus(SiteEntity siteEntity) {
        try {
            siteService.updateStatus(siteEntity.getId(), StatusType.INDEXING, Instant.now());
        } catch (Exception e){
            log.warn("Ошибка изменения статуса INDEXING для сайта: {}", siteEntity.toString());
        }

    }

    @Override
    public SiteEntity saveStatus(SiteEntity siteEntity) {
        try{
            siteService.updateStatus(siteEntity.getId(), StatusType.INDEXED, Instant.now());
            return siteService.findById(siteEntity.getId());

        }catch (NoFoundEntityException e){
            log.warn("Ошибка изменения статуса INDEXED для сайта: {}", siteEntity.getUrl());
            return siteEntity;
        }
    }

    @Override
    public SiteEntity saveStatus(SiteEntity siteEntity, String error) {
        try{
            siteService.updateErrorStatus(siteEntity.getId(),StatusType.FAILED, Instant.now(), error);
            return siteService.findById(siteEntity.getId());
        } catch (NoFoundEntityException e){
            log.warn("Ошибка изменения статуса FAILED для сайта: {}", siteEntity.getUrl());
            return siteEntity;
        }
    }

    @Override
    public Page savePageOrIgnore(PageDto pageDto) {
        Page page = pageMapper.dtoToEntity(pageDto);
        try{
            return pageService.create(page);
        } catch (DataIntegrityViolationException e){
            throw new PageIndexingException("индексации", "Страница уже проиндексирована", pageDto.getSite().getUrl() + pageDto.getPath());
        }
    }

    @Override
    @Transactional
    public SiteEntity createOrRecreateIfExistSite(Site site) {
        String normalizedUrl = normalizeUrl(site.getUrl());
        if(siteService.existsByUrl(normalizedUrl)){
            log.info("Сайт: {} был проиндексирован, удаляем данные.", normalizedUrl);
            siteService.delete(siteService.findByUrl(normalizedUrl));
        }
        return siteService.create(siteMapper.dtoToEntity(site));
    }

    @Override
    public void saveIndexFromDto(SearchIndexDto searchIndexDto) {
            indexService.create(searchIndexMapper.dtoToEntity(searchIndexDto));
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

        try {
            SiteEntity site = siteService.findByUrl(splitLink[0]);
            return pageService.existsByPathAndSite(splitLink[1], site) ||
                    pageService.existsByPathAndSite(normalizeUrl(splitLink[1]), site);
        } catch (NoFoundEntityException e){
            log.warn("Сайт для ссылки: {} не сохранен в БД.", url);
            return false;
        }
    }

    @Override
    @Transactional
    public void deleteNoListSitesFromDb(SitesList sitesList) {
        List<SiteEntity> sites = siteService.findAll();
        for(Site site : sitesList.getSites()){
            sites.removeIf(siteEntity -> siteEntity.getUrl().equals(normalizeUrl(site.getUrl())));
        }
        if(!sites.isEmpty()){
            log.info("Приступаем к удалению сайтов не входящих в список индексируемых из базы");
            StringBuilder builder = new StringBuilder("Список сайтов к удалению: ");
            for(SiteEntity siteEntity : sites){
                builder.append(siteEntity.getUrl()).append(", ");
            }
            log.info(builder.toString());
            siteService.deleteAllByList(sites);
        }

    }

    @Override
    @Transactional
    public void deletePageWithDataByUrl(String url) {
        String normalizedBaseUrl;
        String path;
        try{
            normalizedBaseUrl = normalizeUrl(getBaseUrl(url));
            path = getPath(url);
        } catch (MalformedURLException e) {
            throw new PageIndexingException("индексации", "Адрес ссылки введен неверно.", url);
        }
        try{
            SiteEntity site = siteService.findByUrl(normalizedBaseUrl);
            Page page = pageService.findByPathAndSite(path, site).get(0);
            List<SearchIndex> indexes = page.getIndexes();
            List<Lemma> lemmas = indexes.stream().map(SearchIndex::getLemma).toList();

            for(Lemma lemma : lemmas){
                if(lemma.getFrequency() > 1){
                    lemma.setFrequency(lemma.getFrequency() - 1);
                    lemmaService.update(lemma);
                } else {
                    lemmaService.delete(lemma);
                }
            }
            pageService.delete(page);
            indexService.deleteAllByList(indexes);
        } catch (NoFoundEntityException e){
            log.warn("Страница с ссылкой: {}, отсутствует в базе данных", url);
        }
    }
}
