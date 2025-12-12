package searchengine.web.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.core.components.*;
import searchengine.core.dto.ExtractedDataFromPage;
import searchengine.core.dto.PageDto;
import searchengine.core.utils.HttpStatusCodeRusMessenger;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.exceptions.NoFoundRussianContentException;
import searchengine.exceptions.PageIndexingException;
import searchengine.exceptions.SiteIndexingException;
import searchengine.model.entity.*;
import searchengine.model.services.IndexService;
import searchengine.model.services.LemmaService;
import searchengine.model.services.PageService;
import searchengine.model.services.SiteService;
import searchengine.web.services.IndexingPageService;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static searchengine.core.utils.HtmlUtils.*;
import static searchengine.core.utils.SiteStatusUtils.setFailedStatus;
import static searchengine.core.utils.SiteStatusUtils.setIndexingProcessStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingPageServiceImpl implements IndexingPageService {
    private final ConcurrentHashMap<Long, Object> siteLocks = new ConcurrentHashMap<>();
    private final PageParser pageParser;
    private final SiteService siteService;
    private final PageService pageService;
    private final PageContentExtractor pageContentExtractor;
    private final LemmaService lemmaService;
    private final IndexService indexService;

    @Override
    public List<String> indexing(String link) throws PageIndexingException, SiteIndexingException {

        SiteEntity siteEntity = siteService.getSiteEntityByLink(link);
        siteService.updateStatus(setIndexingProcessStatus(siteEntity));

        PageDto pageDto;
        try {
            pageDto = pageParser.parse(link, siteEntity);
        } catch (SiteIndexingException e) {
            siteService.updateStatus(setFailedStatus(siteEntity, e.getMessage()));
            throw new SiteIndexingException(e.getMessage());
        }
        Page page = pageService.savePageOrIgnore(pageDto);
        if (page.getCode() >= 400 || page.getContent().isEmpty()) {
            log.debug("Пропускаем извлечение дочерних ссылок для страницы: {}", link);
            throw new PageIndexingException(link," ", HttpStatusCodeRusMessenger.getMessageByCode(page.getCode()));
        } else {
            ExtractedDataFromPage dataFromPage;
            try {
                dataFromPage = pageContentExtractor.extract(page);
                saveLemmasAndIndexes(dataFromPage);
                return dataFromPage.getChildLinks();
            } catch (NoFoundRussianContentException e) {
                log.warn(e.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public void saveLemmasAndIndexes(ExtractedDataFromPage data) {
        SiteEntity site = data.getPage().getSite();
        Page page = data.getPage();
        Map<String, Integer> lemmasOnPage = data.getLemmaMap();

        if (lemmasOnPage.isEmpty()) return;

        Object lock = siteLocks.computeIfAbsent(site.getId(), k -> new Object());
        synchronized (lock) {
            lemmaService.updateLemmasForSite(site, lemmasOnPage);
            List<Lemma> savedLemmas = lemmaService.findBySiteAndLemmaIn(site, lemmasOnPage.keySet());
            indexService.createIndexList(page, savedLemmas, lemmasOnPage);
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
            throw new PageIndexingException(url , " Адрес ссылки введен неверно.");
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
