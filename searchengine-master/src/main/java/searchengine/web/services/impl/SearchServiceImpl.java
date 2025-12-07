package searchengine.web.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.core.components.LemmaFinder;
import searchengine.core.components.ResponseBuilder;
import searchengine.core.components.SearchingUtils;
import searchengine.exceptions.SearchingException;
import searchengine.web.services.SearchService;
import searchengine.model.entity.Page;
import searchengine.web.services.dto.responses.SearchResponse;

import java.util.*;
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaFinder lemmaFinder;
    private final SearchingUtils searchingUtils;
    private final ResponseBuilder responseBuilder;

    @Override
    public SearchResponse search(String query, String siteUrl, int offset, int limit){
        if(query.isEmpty()){
            throw new SearchingException("Задан пустой поисковый апрос");
        }
        Set<String> lemmaSet = lemmaFinder.getLemmasSetFromSearch(query);
        List<Page> matchingPages = searchingUtils.getMatchingPages(lemmaSet, siteUrl);

        if (matchingPages.isEmpty()) {
            throw new SearchingException("Ни чего не найдено по вашему запросу");
        }
        Map<Page, Float> absRelMap = searchingUtils.getAbsRelMap(matchingPages, lemmaSet);

        return new SearchResponse(true, matchingPages.size(),
                responseBuilder.buildSearchResultList(absRelMap,offset,limit,lemmaSet));
    }
}
