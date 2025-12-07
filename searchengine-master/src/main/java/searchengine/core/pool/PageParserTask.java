package searchengine.core.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.core.cache.UrlCache;
import searchengine.core.components.PageContentExtractor;
import searchengine.exceptions.PageIndexingException;
import searchengine.model.entity.SiteEntity;
import searchengine.model.services.SiteService;
import searchengine.web.services.IndexingPageService;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

import static searchengine.core.utils.HtmlUtils.timeout;
import static searchengine.core.utils.SiteStatusUtils.setIndexingProcessStatus;

@RequiredArgsConstructor
@Slf4j
public class PageParserTask extends RecursiveAction {
    private final AtomicBoolean indexingRunning;
    private final IndexingPageService indexingPageService;
    private final PageContentExtractor pageContentExtractor;
    private final SiteService siteService;
    private final UrlCache urlCache;
    private final SiteEntity siteEntity;
    private final String url;

    @Override
    protected void compute() {
        if (!indexingRunning.get()) return;
        if(pageContentExtractor.isVisitedLink(url)) return;
        try {
            timeout();
            urlCache.add(url);
            List<String> childLinks;
            try{
                childLinks = indexingPageService.indexing(url);
            } catch (PageIndexingException e){
                log.warn(e.getMessage());
                return;
            }
            List<PageParserTask> parserTasks = childLinks.stream()
                    .map(link -> new PageParserTask(indexingRunning,
                            indexingPageService,
                            pageContentExtractor,
                            siteService,
                            urlCache,
                            siteEntity,
                            link))
                    .toList();
            if (!parserTasks.isEmpty()) invokeAll(parserTasks);
            siteService.updateStatus(setIndexingProcessStatus(siteEntity));

        } catch (RuntimeException e) {
            log.warn("Ошибка при обработке страницы {}: {}", url, e.getMessage());
            throw new CompletionException(e);
        } catch (InterruptedException e ){
            log.warn("Индексация страница {} прервана пользователем", url);
            Thread.currentThread().interrupt();
        }
    }
}
