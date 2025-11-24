package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.core.cache.UrlCache;
import searchengine.core.dto.ExtractedDataFromPage;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.NoFoundRussianContentException;
import searchengine.model.entity.Page;
import searchengine.services.DataProcessorFacade;

import java.util.List;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PageContentExtractor {

    private final LemmaFinder lemmaFinder;
    private final DataProcessorFacade dataProcessorFacade;
    private final UrlCache urlCache;

    public ExtractedDataFromPage extract(Page page){
        ExtractedDataFromPage data = new ExtractedDataFromPage();
        data.setPage(page);
        data.setChildLinks(extractLinks(page.getContent(), page.getSite().getUrl(), page.getPath()));
        try {
            data.setLemmaMap(lemmaFinder.getLemmasMapFromPageContent(page.getContent()));
        } catch (NoFoundRussianContentException e){
            throw new NoFoundRussianContentException("Ошибка извлечения контента для страницы: ",
                    page.getSite().getUrl() + page.getPath(),  " Текст ошибки: ", e.getMessage());
        }
        return data;
    }

    public List<String> extractLinks(String content, String baseUrl, String path) {
        Document document = HtmlUtils.stringToDocument(content, baseUrl);

        List<String> childLinks = document.select("a[href]")
                .stream()
                .map(e -> e.absUrl("href"))
                .filter(HtmlUtils :: isValidLink)
                .filter(b -> !b.equals(baseUrl))
                .filter(l -> !isFile(l))
                .filter(a -> !isAuth(a))
                .filter(e -> isChildLink(e, baseUrl))
                .filter(p -> !isVisitedLink(p))
                .toList();
        if (!childLinks.isEmpty()) {
            log.debug("Извлечено {} дочерних ссылок к сайту [{}]", childLinks.size(),baseUrl + path);
        }
        return childLinks;
    }

    public boolean isVisitedLink(String url){
        return dataProcessorFacade.existsPageLinkInDatabase(url) || !urlCache.shouldProcess(url);
    }
}
