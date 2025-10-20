package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.core.components.LemmaFinder;
import searchengine.core.components.ResponseBuilder;
import searchengine.core.components.SearchingUtils;
import searchengine.services.SearchService;
import searchengine.services.dto.responses.ErrorResponse;
import searchengine.model.entity.Page;
import searchengine.services.dto.responses.SearchResponse;

import java.util.*;
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaFinder lemmaFinder;
    private final SearchingUtils searchingUtils;
    private final ResponseBuilder responseBuilder;

    @Override
    public Object search(String query, String siteUrl, int offset, int limit){
        if(query.isEmpty()){
            return new ErrorResponse(false, "Введен пустой запрос");
        }
        Set<String> lemmaSet = lemmaFinder.getLemmasSetFromSearch(query);
        List<Page> matchingPages = searchingUtils.getMatchingPages(lemmaSet, siteUrl);

        if (matchingPages.isEmpty()) {
            return new SearchResponse(false, 0, List.of());
        }
        Map<Page, Float> absRelMap = searchingUtils.getAbsRelMap(matchingPages, lemmaSet);

        return new SearchResponse(true, matchingPages.size(), responseBuilder.buildSearchResultList(absRelMap,offset,limit,lemmaSet));
    }


}
