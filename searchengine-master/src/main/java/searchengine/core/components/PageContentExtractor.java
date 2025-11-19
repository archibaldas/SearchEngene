package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.core.dto.ExtractedDataFromPage;
import searchengine.core.dto.LemmaDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.exceptions.NoFoundRussianContentException;
import searchengine.model.entity.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PageContentExtractor {
    private final LemmaFinder lemmaFinder;
    private final LinksExtractor linksExtractor;

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

    public ExtractedDataFromPage extract(Page page){
        ExtractedDataFromPage data = new ExtractedDataFromPage();
        data.setPage(page);
        data.setChildLinks(linksExtractor.extract(page.getContent(), page.getSite().getUrl(), page.getPath()));
        try {
            data.setLemmaMap(lemmaFinder.getLemmasMapFromPageContent(page.getContent()));
        } catch (NoFoundRussianContentException e){
            throw new NoFoundRussianContentException("Ошибка извлечения контента для страницы: ",
                    page.getSite().getUrl() + page.getPath(),  " Текст ошибки: ", e.getMessage());
        }
        return data;
    }
}
