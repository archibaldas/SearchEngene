package searchengine.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.core.config.AppContext;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.PageIndexingException;
import searchengine.model.entity.SiteEntity;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
@Slf4j
public class PageParserTask extends RecursiveAction {
    private final AppContext context;
    private final SiteEntity siteEntity;
    private final String url;

    @Override
    protected void compute() {
        if (!context.indexingRunning.get()) return;
        if(context.pageContentExtractor.isVisitedLink(url)) return;
        try {
            HtmlUtils.timeout();
            context.urlCache.add(url);
            List<String> childLinks;
            try{
                childLinks = context.indexingPageService.indexing(url);
            } catch (PageIndexingException e){
                log.warn(e.getMessage());
                return;
            }
            List<PageParserTask> parserTasks = childLinks.stream()
                    .map(link -> new PageParserTask(context, siteEntity, link))
                    .toList();
            if (!parserTasks.isEmpty()) invokeAll(parserTasks);
            context.dataProcessor.processedStatus(siteEntity);

        } catch (RuntimeException e) {
            log.warn("Ошибка при обработке страницы {}: {}", url, e.getMessage());
            throw new CompletionException(e);
        } catch (InterruptedException e ){
            log.warn("Индексация страница {} первана пользователем", url);
            Thread.currentThread().interrupt();
        } finally {
            if(context.dataProcessor.existsPageLinkInDatabase(url)){
                context.urlCache.removeIfExists(url);
            }
        }
    }
}
