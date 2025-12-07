package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.core.cache.UrlCache;
import searchengine.core.dto.ExtractedDataFromPage;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.NoFoundRussianContentException;
import searchengine.exceptions.PageIndexingException;
import searchengine.model.entity.Page;
import searchengine.model.services.PageService;

import java.util.List;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PageContentExtractor {

    private final PageService pageService;
    private final LemmaFinder lemmaFinder;
    private final UrlCache urlCache;

    public ExtractedDataFromPage extract(Page page) throws PageIndexingException{
        ExtractedDataFromPage data = new ExtractedDataFromPage();
        data.setPage(page);
        data.setChildLinks(extractLinks(page.getContent(), page.getSite().getUrl(), page.getPath()));
        try {
            data.setLemmaMap(lemmaFinder.getLemmasMapFromPageContent(page.getContent()));
        } catch (NoFoundRussianContentException e){
            throw new PageIndexingException("[",
                    page.getSite().getUrl(), page.getPath(),"]Ошибка: ", e.getMessage());
        }
        return data;
    }

    private List<String> extractLinks(String content, String baseUrl, String path) {
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
        return pageService.existsPageLinkInDatabase(url) || !urlCache.shouldProcess(url);
    }
}
