package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;
import searchengine.model.services.SiteService;
import searchengine.services.DataProcessorFacade;
import searchengine.services.IndexingPageService;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingPageServiceImpl implements IndexingPageService {
    private final SitesList sites;
    private final PageParser pageParser;
    private final SiteService siteService;
    private final SiteMapper siteMapper;
    private final DataProcessorFacade dataProcessor;
    private final PageContentExtractor pageContentExtractor;

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
                setIndexesFromData(dataFromPage);
                return dataFromPage.getChildLinks();
            } catch (NoFoundRussianContentException e){
                log.warn(e.getMessage());
                return new ArrayList<>();
            }
        }
    }

    private void setIndexesFromData(ExtractedDataFromPage data) {
        List<SearchIndexDto> indexes = new ArrayList<>();
        Map<String, Integer> lemmas = data.getLemmaMap();
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            LemmaDto lemma = new LemmaDto();
            lemma.setSite(data.getPage().getSite());
            lemma.setLemma(entry.getKey());
            SearchIndexDto index = new SearchIndexDto();
            index.setPage(data.getPage());
            index.setLemma(lemma);
            index.setRank(entry.getValue().floatValue());
            indexes.add(index);
        }

        if(!indexes.isEmpty()){
            for(SearchIndexDto searchIndexDto : indexes){
                dataProcessor.saveIndexFromDto(searchIndexDto);
            }
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
