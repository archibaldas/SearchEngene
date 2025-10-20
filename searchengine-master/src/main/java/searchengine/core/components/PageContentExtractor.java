package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.core.dto.LemmaDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.model.entity.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PageContentExtractor {
    private final LemmaFinder lemmaFinder;

    public List<SearchIndexDto> getIndexesFromPage(Page page) {
        List<SearchIndexDto> indexes = new ArrayList<>();
        Map<String, Integer> lemmas = lemmaFinder.getLemmasMapFromPageContent(page.getContent());
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            LemmaDto lemma = new LemmaDto();
            lemma.setSite(page.getSite());
            lemma.setLemma(entry.getKey());
            SearchIndexDto index = new SearchIndexDto();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRank(entry.getValue().floatValue());
            indexes.add(index);
        }
        return indexes;
    }

}
