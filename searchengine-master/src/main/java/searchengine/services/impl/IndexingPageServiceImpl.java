package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.core.components.*;
import searchengine.core.dto.ExtractedDataFromPage;
import searchengine.core.dto.LemmaDto;
import searchengine.core.dto.PageDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.core.utils.HttpStatusCodeRusMessenger;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.exceptions.NoFoundRussianContentException;
import searchengine.exceptions.PageIndexingException;
import searchengine.mapper.SiteMapper;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;
import searchengine.model.entity.SiteEntity;
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.repositories.SearchIndexRepository;
import searchengine.model.services.LemmaService;
import searchengine.model.services.SiteService;
import searchengine.services.DataProcessorFacade;
import searchengine.services.IndexingPageService;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingPageServiceImpl implements IndexingPageService {
    private final ConcurrentHashMap<Long, Object> siteLocks = new ConcurrentHashMap<>();
    private final SitesList sites;
    private final PageParser pageParser;
    private final SiteService siteService;
    private final SiteMapper siteMapper;
    private final DataProcessorFacade dataProcessor;
    private final PageContentExtractor pageContentExtractor;
    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final SearchIndexRepository indexRepository;

    @Override
    public List<String> indexing(String link) {

        SiteEntity siteEntity = getSiteEntity(link);

        dataProcessor.processedStatus(siteEntity);

        PageDto pageDto = pageParser.parse(link, siteEntity);
        Page page = dataProcessor.savePageOrIgnore(pageDto);
        if(page.getCode() >= 400 || page.getContent().isEmpty()){
            log.warn("Пропускаем извлечение дочерних ссылок для страницы: {}", link);
            throw new PageIndexingException("индексации", HttpStatusCodeRusMessenger.getMessageByCode(page.getCode()), link);
        }else {
            ExtractedDataFromPage dataFromPage;
            try {
                dataFromPage = pageContentExtractor.extract(page);
                saveLemmasAndIndexes(null, dataFromPage);
//                setIndexesFromData(dataFromPage);
                return dataFromPage.getChildLinks();
            } catch (NoFoundRussianContentException e){
                log.warn(e.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public void saveLemmasAndIndexes(List<SearchIndexDto> unusedDtos, ExtractedDataFromPage data) {
        SiteEntity site = data.getPage().getSite();
        Page page = data.getPage();
        Map<String, Integer> lemmasOnPage = data.getLemmaMap();

        if(lemmasOnPage.isEmpty()) return;

        Object lock = siteLocks.computeIfAbsent(site.getId(), k -> new Object());
        synchronized (lock) {
            try{
                lemmaService.updateLemmasForSite(site, lemmasOnPage);
            } catch (DataIntegrityViolationException e){
                log.warn("Пойман дубликат пробуем поторно ...");
                lemmaService.updateLemmasForSite(site, lemmasOnPage);
            }

            List<Lemma> savedLemmas = lemmaRepository.findBySiteAndLemmaIn(site, lemmasOnPage.keySet());
            List<SearchIndex> indexesToSave = new ArrayList<>();
        for(Lemma savedLemma : savedLemmas){
            String dbLemmaText = savedLemma.getLemma();


            Integer rank = lemmasOnPage.get(dbLemmaText);
            if(rank == null){
                log.warn("Лемма '{}' из БД не найдена в карте страницы. Пропуск индексации.", dbLemmaText);
                continue;
            }
            SearchIndex index = new SearchIndex();
            index.setPage(page);
            index.setLemma(savedLemma);
            index.setRank(rank.floatValue());

            indexesToSave.add(index);
        }

        indexRepository.saveAllAndFlush(indexesToSave);
        }
    }

    private SiteEntity getSiteEntity(String link) throws PageIndexingException{
        String baseUrl;
        Site site = null;
        try{
            baseUrl = getBaseUrl(link);
            for(Site s : sites.getSites()){
                if(equalsHosts(s.getUrl(), baseUrl)){
                    site = s;
                }
            }
        } catch (MalformedURLException e) {
            throw new PageIndexingException("индексации", "Адрес ссылки введен неверно.", link);
        }
        if(site == null) {
            throw new PageIndexingException("индексации", "Сайт по ссылке отсутсвует в списке индексируемых сайтов", link);
        }
        try {
            return siteService.findByUrl(normalizeUrl(site.getUrl()));
        } catch (NoFoundEntityException e){
            log.warn("Ошибка: {}", e.getMessage());
            log.info("Запись сайта [{}] в БД" , site.getUrl());
            return siteService.create(siteMapper.dtoToEntity(site));
        }
    }
}
