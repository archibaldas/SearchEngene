package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.core.dto.PageDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.mapper.PageMapper;
import searchengine.mapper.SearchIndexMapper;
import searchengine.mapper.SiteMapper;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;
import searchengine.model.entity.SiteEntity;
import searchengine.model.services.*;
import searchengine.services.DataProcessorFacade;

import java.net.MalformedURLException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessorFacadeImpl implements DataProcessorFacade {

    private final LemmaService lemmaService;

    private final SiteService siteService;
    private final SiteMapper siteMapper;
    private final PageService pageService;
    private final PageMapper pageMapper;
    private final SearchIndexMapper searchIndexMapper;
    private final IndexService indexService;


    @Override
    public SiteEntity findOrCreateSiteInDatabase(Site site) {
        try{
            return siteService.findByUrl(site.getUrl());
        }  catch (NoFoundEntityException e) {
            return siteService.create(siteMapper.dtoToEntity(site));
        }
    }

    @Override
    public void processedStatus(SiteEntity siteEntity) {
        try {
            siteService.update(siteMapper.setIndexing(siteEntity));
        } catch (NoFoundEntityException e){
            log.warn("Ошибка изменения статуса INDEXING для сайта: {}", siteEntity.toString());
        }

    }

    @Override
    public SiteEntity saveStatus(SiteEntity siteEntity) {
        try{
            return siteService.update(siteMapper.setIndexed(siteEntity));

        }catch (NoFoundEntityException e){
            log.warn("Ошибка изменения статуса INDEXED для сайта: {}", siteEntity.toString());
            return siteEntity;
        }
    }

    @Override
    public SiteEntity saveStatus(SiteEntity siteEntity, String error) {
        try{
            return siteService.update(siteMapper.setFailed(siteEntity, error));
        } catch (NoFoundEntityException e){
            log.warn("Ошибка изменения статуса FAILED для сайта: {}", siteEntity.toString());
            return siteEntity;
        }
    }

    @Override
    public Page saveOrUpdatePageToDatabase(PageDto pageDto) {
        Page page = pageMapper.dtoToEntity(pageDto);
        try{
            page = pageService.findByPathAndSite(page.getPath(), page.getSite());
            return pageService.update(page);
        } catch (NoFoundEntityException e){
            return pageService.create(page);
        }
    }

    @Override
    public void deleteAllBySite(Site site) {
        try {
            SiteEntity siteEntity = siteService.findByUrl(HtmlUtils.normalizeUrl(site.getUrl()));
            List<Page> pages = pageService.findAllBySite(siteEntity);
            List<Lemma> lemmas = lemmaService.findAllBySite(siteEntity);
            lemmas.forEach(lemmaService::delete);
            pages.forEach(p -> {
                List<SearchIndex> indexes = indexService.findAllByPage(p);
                indexes.forEach(indexService::delete);
                pageService.delete(p);
            });
            siteService.delete(siteEntity);
        } catch (NoFoundEntityException e){
            log.debug("Данные сайта: {} не найдены в базе данных. Удаление не требуется", site.getUrl());
        }
    }

    @Override
    public void saveIndexFromDto(SearchIndexDto searchIndexDto) {
            indexService.create(searchIndexMapper.dtoToEntity(searchIndexDto));
    }

    @Override
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
            return pageService.existsByPathAndSite(splitLink[1], site);
        } catch (NoFoundEntityException e){
//            log.debug("Нет данных по данному Url: {}", url);
            return false;
        }
    }
}
